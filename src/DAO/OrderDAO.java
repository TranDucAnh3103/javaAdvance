package DAO;

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
}
