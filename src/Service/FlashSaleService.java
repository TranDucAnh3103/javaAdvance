package Service;

import Models.MarketingPromotions.FlashSale;
import Models.MarketingPromotions.FlashSaleItem;
import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface nghiệp vụ Flash Sale.
 * Tách biệt logic validate (Service) khỏi thao tác DB (DAO).
 */
public interface FlashSaleService {

    /** Admin: Tạo sự kiện Flash Sale mới, trả về ID. */
    int createFlashSale(FlashSale flashSale) throws InvalidInputException, DatabaseException;

    /** Admin: Gắn 1 variant vào Flash Sale. */
    boolean addFlashSaleItem(FlashSaleItem item) throws InvalidInputException, DatabaseException;

    /** Lấy danh sách Flash Sale đang diễn ra. */
    List<FlashSale> getActiveFlashSales() throws DatabaseException;

    /** Lấy tất cả Flash Sale (Admin review). */
    List<FlashSale> getAllFlashSales() throws DatabaseException;

    /** Lấy danh sách SP tham gia 1 sự kiện. */
    List<FlashSaleItem> getFlashSaleItems(int flashSaleId) throws DatabaseException;

    /**
     * Kiểm tra variant có đang Flash Sale không.
     * Nếu có → trả FlashSaleItem (chứa % giảm).
     * Nếu không → trả null.
     */
    FlashSaleItem getActiveFlashSaleItem(int variantId) throws DatabaseException;

    /**
     * Tính giá Flash Sale cho 1 variant.
     * Công thức: giá_gốc × (1 - discount_percent / 100)
     * Nếu variant không tham gia Flash Sale → trả về giá gốc.
     */
    BigDecimal calculateFlashPrice(BigDecimal originalPrice, int variantId) throws DatabaseException;
}
