package Models.DTO;

import java.math.BigDecimal;

public class CartItem {
    private int variantId;
    private int productId;
    private String productName;
    private String specs; // Ví dụ: "8GB/128GB - Đỏ"
    private BigDecimal unitPrice;
    private int quantity;

    public CartItem() {
    }

    public CartItem(int variantId, int productId, String productName, String specs, BigDecimal unitPrice, int quantity) {
        this.variantId = variantId;
        this.productId = productId;
        this.productName = productName;
        this.specs = specs;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    // Tính tổng tiền của item này
    public BigDecimal getTotalPrice() {
        return this.unitPrice.multiply(new BigDecimal(this.quantity));
    }
}
