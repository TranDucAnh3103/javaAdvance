package Service;

import Models.SalesOrders.Order;
import Models.DTO.CartItem;
import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;

import java.util.List;

public interface OrderService {
    /**
     * Đặt hàng và thanh toán giỏ hàng.
     */
    boolean checkout(int userId, String shippingAddress, List<CartItem> cart) throws InvalidInputException, DatabaseException;

    /**
     * Xem danh sách hóa đơn theo ID Khách hàng
     */
    List<Order> getOrderHistory(int userId) throws DatabaseException;
}
