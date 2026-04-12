package Models.SalesOrders;

import java.math.BigDecimal; // Xử lý số thực độ chính xác cao, chống sai số khi tính toán tiền tệ.
import java.sql.Timestamp; // Thư viện mở rộng từ java.util.Date, tương thích hoàn toàn với kiểu TIMESTAMP của SQL.

public class Order {
    public enum OrderStatus { PENDING, SHIPPING, DELIVERED, CANCELLED }

    private int id;
    private int userId;
    private Integer couponId; // Dùng Integer để chấp nhận null
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String shippingAddress;
    private Timestamp createdAt;

    // Field phụ trợ: Chỉ dùng khi Admin xem danh sách đơn hàng (JOIN bảng users lấy tên).
    // Không nằm trong bảng orders, không INSERT/UPDATE — thuần hiển thị.
    private String customerName;

    public Order() {}

    public Order(Integer couponId, Timestamp createdAt, int id, String shippingAddress, OrderStatus status, BigDecimal totalAmount, int userId) {
        this.couponId = couponId;
        this.createdAt = createdAt;
        this.id = id;
        this.shippingAddress = shippingAddress;
        this.status = status;
        this.totalAmount = totalAmount;
        this.userId = userId;
    }

    public Integer getCouponId() {
        return couponId;
    }

    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public String toString() {
        return "Order{" +
                "couponId=" + couponId +
                ", id=" + id +
                ", userId=" + userId +
                ", customerName='" + customerName + '\'' +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
