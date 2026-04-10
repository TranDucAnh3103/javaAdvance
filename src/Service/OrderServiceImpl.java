package Service;

import DAO.OrderDAO;
import DAO.OrderDAOImpl;
import Models.SalesOrders.Order;
import Models.DTO.CartItem;
import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;

import java.math.BigDecimal;
import java.util.List;

public class OrderServiceImpl implements OrderService {

    private OrderDAO orderDAO = new OrderDAOImpl();

    @Override
    public boolean checkout(int userId, String shippingAddress, List<CartItem> cart) throws InvalidInputException, DatabaseException {
        // Validation lớp đầu (Biz rules)
        if (userId <= 0) {
            throw new InvalidInputException("Lỗi định danh tài khoản, vui lòng đăng nhập lại.");
        }
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new InvalidInputException("Địa chỉ giao hàng không được để trống.");
        }
        if (cart == null || cart.isEmpty()) {
            throw new InvalidInputException("Giỏ hàng của bạn đang trống! Hãy quay lại chọn sản phẩm.");
        }

        // Tính tổng tiền dựa trên số lượng x đơn giá của từng item trong list memory
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cart) {
            if (item.getQuantity() <= 0) {
                 throw new InvalidInputException("Sản phẩm [" + item.getProductName() + "] có số lượng mua = 0. Vui lòng căn chỉnh lại!");
            }
            totalAmount = totalAmount.add(item.getTotalPrice());
        }

        // Setup thực thể Order
        Order order = new Order();
        order.setUserId(userId);
        order.setShippingAddress(shippingAddress.trim());
        order.setTotalAmount(totalAmount);
        order.setStatus(Order.OrderStatus.PENDING); // Mới đặt thì trạng thái Chờ xác nhận

        // Ủy thác xuống DAO
        try {
            return orderDAO.createOrder(order, cart);
        } catch (Exception e) {
            // DAO ném lỗi văng lên đây (Đứt mạng, thiếu Tồn kho)
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    @Override
    public List<Order> getOrderHistory(int userId) throws DatabaseException {
        try {
            return orderDAO.getOrdersByUserId(userId);
        } catch (Exception e) {
            throw new DatabaseException("Lỗi lấy lịch sử đơn hàng từ CSDL: " + e.getMessage(), e);
        }
    }
}
