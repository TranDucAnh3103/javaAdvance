package Models.DTO;

import java.math.BigDecimal;

public class ProductDTO {
    private String categoryName;
    private int productId;
    private String productName;
    private String brand;
    private int variantId;
    private String ram;
    private String rom;
    private String color;
    private BigDecimal price;
    private int stock;

    public ProductDTO() {
    }

    public ProductDTO(String categoryName, int productId, String productName, String brand, int variantId, String ram, String rom, String color, BigDecimal price, int stock) {
        this.categoryName = categoryName;
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.variantId = variantId;
        this.ram = ram;
        this.rom = rom;
        this.color = color;
        this.price = price;
        this.stock = stock;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getRom() {
        return rom;
    }

    public void setRom(String rom) {
        this.rom = rom;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "categoryName='" + categoryName + '\'' +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", brand='" + brand + '\'' +
                ", variantId=" + variantId +
                ", ram='" + ram + '\'' +
                ", rom='" + rom + '\'' +
                ", color='" + color + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }
}
