package Models.SalesOrders;

import java.math.BigDecimal; // Xử lý số thực độ chính xác cao, chống sai số khi tính toán tiền tệ.

public class OrderDetail {
    private int orderId;
    private int variantId;
    private int quantity;
    private BigDecimal unitPrice; // Lưu giá tại thời điểm mua

    public OrderDetail() {}

    public OrderDetail(int orderId, int quantity, BigDecimal unitPrice, int variantId) {
        this.orderId = orderId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.variantId = variantId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    @Override
    public String toString() {
        return "OrderDetail{" +
                "orderId=" + orderId +
                ", variantId=" + variantId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }
}
