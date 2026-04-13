package Service.Sales;

import DAO.Sales.OrderDAO;
import DAO.Sales.OrderDAOImpl;
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

    @Override
    public List<Order> getAllOrders() throws DatabaseException {
        try {
            return orderDAO.getAllOrders();
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi tải danh sách đơn hàng: " + e.getMessage(), e);
        }
    }

    /**
     * Logic chuyển trạng thái đơn hàng hoạt động theo kiểu "máy trạng thái 1 chiều":
     *   PENDING → SHIPPING → DELIVERED   (luồng chính — đơn hàng bình thường)
     *   PENDING → CANCELLED              (huỷ đơn — chỉ được hủy khi chưa giao)
     *   SHIPPING → CANCELLED             (huỷ đơn giữa chừng — tuỳ chính sách cửa hàng)
     *
     * Tại sao không cho phép quay đầu?
     *   - DELIVERED → SHIPPING: Hàng giao rồi, không thể "chưa giao" lại.
     *   - CANCELLED → bất kì: Đơn đã hủy thì coi như xong, muốn mua lại thì đặt đơn mới.
     */
    @Override
    public boolean updateOrderStatus(int orderId, String newStatus) throws InvalidInputException, DatabaseException {
        // Bước 1: Kiểm tra trạng thái mới có nằm trong danh sách cho phép không
        try {
            Order.OrderStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Trạng thái '" + newStatus + "' không tồn tại! Chỉ chấp nhận: PENDING, SHIPPING, DELIVERED, CANCELLED.");
        }

        try {
            // Bước 2: Kéo đơn hàng hiện tại lên để biết nó đang ở trạng thái nào
            List<Order> allOrders = orderDAO.getAllOrders();
            Order targetOrder = null;
            for (Order o : allOrders) {
                if (o.getId() == orderId) {
                    targetOrder = o;
                    break;
                }
            }

            if (targetOrder == null) {
                throw new InvalidInputException("Không tìm thấy đơn hàng có mã #" + orderId);
            }

            String currentStatus = targetOrder.getStatus().name();

            // Bước 3: Kiểm tra xem việc chuyển từ trạng thái hiện tại sang trạng thái mới có hợp lệ không
            if (!isValidTransition(currentStatus, newStatus)) {
                throw new InvalidInputException(
                    "Không thể chuyển đơn #" + orderId + " từ [" + currentStatus + "] sang [" + newStatus + "]. " +
                    "Quy trình hợp lệ: PENDING → SHIPPING → DELIVERED, hoặc HỦY khi chưa giao xong."
                );
            }

            return orderDAO.updateOrderStatus(orderId, newStatus);
        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Lỗi cập nhật trạng thái đơn hàng: " + e.getMessage(), e);
        }
    }

    /**
     * Bảng chuyển trạng thái hợp lệ.
     * Đọc theo chiều: "Từ trạng thái A, CÓ THỂ chuyển sang B không?"
     */
    private boolean isValidTransition(String from, String to) {
        // Nếu giữ nguyên trạng thái → không cần update, nhưng cũng không báo lỗi
        if (from.equals(to)) return false;

        return switch (from) {
            case "PENDING"   -> to.equals("SHIPPING") || to.equals("CANCELLED");  // Chưa giao → Bắt đầu giao HOẶC Hủy
            case "SHIPPING"  -> to.equals("DELIVERED") || to.equals("CANCELLED"); // Đang giao → Đã giao HOẶC Hủy giữa chừng
            case "DELIVERED"  -> false; // Đã giao rồi thì xong, không quay đầu
            case "CANCELLED" -> false;  // Đã hủy thì chốt luôn
            default -> false;
        };
    }
}
