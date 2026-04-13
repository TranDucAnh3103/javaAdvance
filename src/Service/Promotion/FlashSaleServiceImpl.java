package Service.Promotion;

import DAO.Promotion.FlashSaleDAO;
import DAO.Promotion.FlashSaleDAOImpl;
import Models.MarketingPromotions.FlashSale;
import Models.MarketingPromotions.FlashSaleItem;
import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Xử lý nghiệp vụ Flash Sale.
 * Tầng này lo validate đầu vào + tính toán giá, rồi ủy thác DB xuống DAO.
 */
public class FlashSaleServiceImpl implements FlashSaleService {

    private FlashSaleDAO flashSaleDAO = new FlashSaleDAOImpl();

    /**
     * Tạo sự kiện Flash Sale mới.
     * Validate: Tên không trống, thời gian bắt đầu phải trước kết thúc.
     * Tại sao phải check start < end? Vì nếu ngược lại, câu WHERE "NOW() BETWEEN start AND end"
     * ở DAO sẽ không bao giờ match → sự kiện tồn tại nhưng chẳng bao giờ active.
     */
    @Override
    public int createFlashSale(FlashSale flashSale) throws InvalidInputException, DatabaseException {
        if (flashSale.getName() == null || flashSale.getName().trim().isEmpty()) {
            throw new InvalidInputException("Tên sự kiện Flash Sale không được để trống!");
        }
        if (flashSale.getStartTime() == null || flashSale.getEndTime() == null) {
            throw new InvalidInputException("Thời gian bắt đầu và kết thúc không được để trống!");
        }
        // Nếu start_time >= end_time thì sự kiện không có ý nghĩa (thời lượng = 0 hoặc âm)
        if (!flashSale.getStartTime().before(flashSale.getEndTime())) {
            throw new InvalidInputException("Thời gian bắt đầu phải TRƯỚC thời gian kết thúc!");
        }
        try {
            return flashSaleDAO.createFlashSale(flashSale);
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi tạo Flash Sale: " + e.getMessage(), e);
        }
    }

    /**
     * Thêm SP vào Flash Sale.
     * Validate: % giảm phải nằm trong khoảng (0, 100], sale_stock > 0.
     * Tại sao không cho 0%? Giảm giá 0% thì chẳng giảm gì — gây hiểu nhầm cho Customer.
     * Tại sao không cho > 100%? Vì bạn không muốn phải TRẢ tiền cho khách khi họ mua hàng đâu :)
     */
    @Override
    public boolean addFlashSaleItem(FlashSaleItem item) throws InvalidInputException, DatabaseException {
        if (item.getDiscountPercent() == null ||
                item.getDiscountPercent().compareTo(BigDecimal.ZERO) <= 0 ||
                item.getDiscountPercent().compareTo(new BigDecimal("100")) > 0) {
            throw new InvalidInputException("Phần trăm giảm giá phải nằm trong khoảng (0% - 100%]!");
        }
        if (item.getSaleStock() <= 0) {
            throw new InvalidInputException("Số lượng dành cho Flash Sale phải > 0!");
        }
        try {
            return flashSaleDAO.addFlashSaleItem(item);
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi thêm SP vào Flash Sale: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FlashSale> getActiveFlashSales() throws DatabaseException {
        try {
            return flashSaleDAO.getActiveFlashSales();
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi lấy danh sách Flash Sale đang diễn ra: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FlashSale> getAllFlashSales() throws DatabaseException {
        try {
            return flashSaleDAO.getAllFlashSales();
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi lấy toàn bộ Flash Sale: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FlashSaleItem> getFlashSaleItems(int flashSaleId) throws DatabaseException {
        try {
            return flashSaleDAO.getFlashSaleItems(flashSaleId);
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi lấy danh sách SP Flash Sale: " + e.getMessage(), e);
        }
    }

    @Override
    public FlashSaleItem getActiveFlashSaleItem(int variantId) throws DatabaseException {
        try {
            return flashSaleDAO.getActiveFlashSaleItem(variantId);
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi kiểm tra Flash Sale cho variant: " + e.getMessage(), e);
        }
    }

    /**
     * Tính giá Flash Sale.
     * Ví dụ: Giá gốc = 20,000,000 VNĐ, giảm 15% → 20,000,000 × (1 - 0.15) = 17,000,000 VNĐ.
     * Dùng BigDecimal để tránh mất chính xác khi tính toán tiền — float/double sẽ bị vấn đề
     * kiểu 19999999.99999 thay vì 20000000.
     */
    @Override
    public BigDecimal calculateFlashPrice(BigDecimal originalPrice, int variantId) throws DatabaseException {
        FlashSaleItem flashItem = getActiveFlashSaleItem(variantId);
        if (flashItem == null) {
            return originalPrice; // Không tham gia Flash Sale → giữ giá gốc
        }
        // Công thức tính: giá_gốc × (100 - discount%) / 100
        BigDecimal discountMultiplier = BigDecimal.valueOf(100).subtract(flashItem.getDiscountPercent())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return originalPrice.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
