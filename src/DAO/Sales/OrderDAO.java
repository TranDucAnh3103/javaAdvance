package DAO.Sales;

import Models.SalesOrders.Order;
import Models.DTO.CartItem;

import java.util.List;

public interface OrderDAO {
    /**
     * Tạo hóa đơn mới. (Tích hợp Transaction)
     */
    boolean createOrder(Order order, List<CartItem> cart) throws Exception;

    /**
     * Lấy lịch sử mua hàng của khách.
     */
    List<Order> getOrdersByUserId(int userId) throws Exception;

    /**
     * Admin: Kéo toàn bộ đơn hàng của mọi khách hàng (JOIN users lấy tên người mua).
     */
    List<Order> getAllOrders() throws Exception;

    /**
     * Admin: Cập nhật trạng thái đơn hàng (PENDING → SHIPPING → DELIVERED / CANCELLED).
     * Trả về true nếu update thành công (tồn tại orderId và câu lệnh ảnh hưởng >= 1 row).
     */
    boolean updateOrderStatus(int orderId, String newStatus) throws Exception;
}
