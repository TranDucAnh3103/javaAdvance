package DAO;

import Models.MarketingPromotions.FlashSale;
import Models.MarketingPromotions.FlashSaleItem;
import Models.DTO.ProductDTO;

import java.util.List;

/**
 * Interface quản lý Flash Sale — sự kiện giảm giá theo thời gian.
 * Hai bảng liên quan: flash_sales (khung sự kiện) và flash_sale_items (SP tham gia).
 */
public interface FlashSaleDAO {

    /**
     * Tạo sự kiện Flash Sale mới (Admin nhập tên, thời gian bắt đầu/kết thúc).
     * Trả về ID tự sinh của sự kiện vừa tạo — cần ID này để thêm SP vào sự kiện.
     */
    int createFlashSale(FlashSale flashSale) throws Exception;

    /**
     * Gắn 1 sản phẩm (variant) vào 1 sự kiện Flash Sale.
     * Mỗi variant chỉ nên tham gia 1 Flash Sale tại 1 thời điểm (tránh xung đột giá).
     */
    boolean addFlashSaleItem(FlashSaleItem item) throws Exception;

    /**
     * Lấy danh sách các sự kiện Flash Sale đang diễn ra (thời gian hiện tại nằm giữa start_time và end_time).
     */
    List<FlashSale> getActiveFlashSales() throws Exception;

    /**
     * Lấy tất cả sự kiện Flash Sale — kể cả đã kết thúc (Admin review lại lịch sử).
     */
    List<FlashSale> getAllFlashSales() throws Exception;

    /**
     * Lấy danh sách sản phẩm tham gia 1 sự kiện Flash Sale cụ thể.
     */
    List<FlashSaleItem> getFlashSaleItems(int flashSaleId) throws Exception;

    /**
     * Kiểm tra xem 1 variant có đang nằm trong Flash Sale nào không.
     * Nếu có → trả về FlashSaleItem chứa thông tin % giảm giá + sale_stock.
     * Nếu không → trả về null.
     * Dùng khi Customer xem sản phẩm / checkout để tính giá Flash.
     */
    FlashSaleItem getActiveFlashSaleItem(int variantId) throws Exception;

    /**
     * Khi Customer checkout hàng Flash Sale → trừ sale_stock tương ứng.
     * Lưu ý: stock bên product_variants cũng phải bị trừ đồng thời (Dual Stock Deduction).
     * Hàm này CHỈ trừ sale_stock. Việc trừ stock chính được xử lý ở OrderDAO.
     */
    boolean deductSaleStock(int flashSaleId, int variantId, int quantity) throws Exception;
}
