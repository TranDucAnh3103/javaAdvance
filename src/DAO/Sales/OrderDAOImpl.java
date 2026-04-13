package DAO.Sales;

import Models.SalesOrders.Order;
import Models.DTO.CartItem;
import Util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDAOImpl implements OrderDAO {

    /**
     * Sử dụng kỹ thuật Database Transaction.
     * Cần phải chạy liên hoàn 3 câu lệnh thay đổi dữ liệu mà không được đứt gánh giữa đường.
     */
    @Override
    public boolean createOrder(Order order, List<CartItem> cart) throws Exception {
        String insertOrderSql = "INSERT INTO orders (user_id, coupon_id, total_amount, shipping_address, status) VALUES (?, ?, ?, ?, 'PENDING')";
        String insertOrderDetailSql = "INSERT INTO order_details (order_id, variant_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        String updateVariantStockSql = "UPDATE product_variants SET stock = stock - ? WHERE id = ? AND stock >= ?";

        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false); // Bắt đầu Transaction

            int orderId = -1;

            // 1. Insert Order lấy ID tự sinh
            try (PreparedStatement psOrder = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, order.getUserId());
                if (order.getCouponId() != null) {
                    psOrder.setInt(2, order.getCouponId());
                } else {
                    psOrder.setNull(2, java.sql.Types.INTEGER);
                }
                psOrder.setBigDecimal(3, order.getTotalAmount());
                psOrder.setString(4, order.getShippingAddress());
                
                int affectedRows = psOrder.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Khởi tạo hóa đơn thất bại.");
                }

                try (ResultSet generatedKeys = psOrder.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Không lấy được ID vòng lặp hóa đơn.");
                    }
                }
            }

            // 2. Loop qua CartItem để insert order_details và GIẢM TRỪ STOCK
            try (PreparedStatement psDetail = con.prepareStatement(insertOrderDetailSql);
                 PreparedStatement psUpdateStock = con.prepareStatement(updateVariantStockSql)) {
                 
                for (CartItem item : cart) {
                    // a) Cập nhật Stock trước. (Mệnh đề WHERE ... AND stock >= quantity giúp chặn lỗi bán vượt kho)
                    psUpdateStock.setInt(1, item.getQuantity());
                    psUpdateStock.setInt(2, item.getVariantId());
                    psUpdateStock.setInt(3, item.getQuantity()); 
                    
                    int rowsUpdated = psUpdateStock.executeUpdate();
                    if (rowsUpdated == 0) {
                        // Stock không đủ hoặc mặt hàng vừa bị ai đó mua sạch -> Phá vỡ Transaction!
                        throw new SQLException("Sản phẩm [" + item.getProductName() + "] hiện không đủ " + item.getQuantity() + " máy trong kho. Vui lòng cập nhật lại giỏ hàng!");
                    }

                    // b) Insert Order Detail
                    psDetail.setInt(1, orderId);
                    psDetail.setInt(2, item.getVariantId());
                    psDetail.setInt(3, item.getQuantity());
                    psDetail.setBigDecimal(4, item.getUnitPrice());
                    psDetail.executeUpdate();
                }
            }

            con.commit(); // Mọi việc suôn sẻ -> Chấp thuận lưu vĩnh viễn!
            return true;
        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback(); // Dọn dẹp chiến trường nếu có exception sinh ra
                } catch (SQLException ex) {
                    throw new Exception("Lỗi khi rollback rác dữ liệu: " + ex.getMessage());
                }
            }
            throw e; 
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    @Override
    public List<Order> getOrdersByUserId(int userId) throws Exception {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order();
                    o.setId(rs.getInt("id"));
                    o.setUserId(rs.getInt("user_id"));
                    int couponId = rs.getInt("coupon_id");
                    if (!rs.wasNull()) { o.setCouponId(couponId); }
                    o.setTotalAmount(rs.getBigDecimal("total_amount"));
                    o.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                    o.setShippingAddress(rs.getString("shipping_address"));
                    o.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(o);
                }
            }
        }
        return list;
    }

    /**
     * Admin xem tất cả đơn hàng — JOIN bảng users để biết ai đặt đơn nào.
     * Nếu chỉ SELECT mỗi bảng orders thì Admin chỉ thấy user_id (số), chẳng biết ai là ai.
     */
    @Override
    public List<Order> getAllOrders() throws Exception {
        List<Order> list = new ArrayList<>();
        // LEFT JOIN users để lấy tên khách hàng. Dùng LEFT JOIN phòng trường hợp tài khoản user bị xóa
        // nhưng đơn hàng vẫn còn trong hệ thống (không mất dữ liệu giao dịch).
        String sql = "SELECT o.*, u.full_name FROM orders o LEFT JOIN users u ON o.user_id = u.id ORDER BY o.created_at DESC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order o = new Order();
                o.setId(rs.getInt("id"));
                o.setUserId(rs.getInt("user_id"));
                o.setCustomerName(rs.getString("full_name")); // Tên khách đi kèm để Admin nhìn cho dễ
                int couponId = rs.getInt("coupon_id");
                if (!rs.wasNull()) { o.setCouponId(couponId); }
                o.setTotalAmount(rs.getBigDecimal("total_amount"));
                o.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                o.setShippingAddress(rs.getString("shipping_address"));
                o.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(o);
            }
        }
        return list;
    }

    /**
     * Đổi trạng thái đơn hàng.
     * Lưu ý: Ở tầng DAO ta chỉ lo chạy câu UPDATE thôi. Việc kiểm tra "có được phép chuyển trạng thái
     * từ A sang B không" thuộc về tầng Service (business logic). DAO không quan tâm luật.
     */
    @Override
    public boolean updateOrderStatus(int orderId, String newStatus) throws Exception {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0; // Trả true nếu có ít nhất 1 row bị ảnh hưởng (tức đơn hàng tồn tại)
        }
    }
}
