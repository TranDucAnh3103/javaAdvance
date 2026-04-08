package Models.CoreEntities;

import java.math.BigDecimal; // Xử lý số thực độ chính xác cao, chống sai số khi tính toán tiền tệ.

public class ProductVariant {
    private int id;
    private int productId;
    private String ram;
    private String rom;
    private String color;
    private BigDecimal price; // Sử dụng BigDecimal cho tiền tệ để tránh sai số
    private int stock;
    private boolean isDeleted;

    public ProductVariant() {}

    public ProductVariant(String color, int id, boolean isDeleted, BigDecimal price, int productId, String ram, String rom, int stock) {
        this.color = color;
        this.id = id;
        this.isDeleted = isDeleted;
        this.price = price;
        this.productId = productId;
        this.ram = ram;
        this.rom = rom;
        this.stock = stock;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
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

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }


    @Override
    public String toString() {
        return "ProductVariant{" +
                "color='" + color + '\'' +
                ", id=" + id +
                ", productId=" + productId +
                ", ram='" + ram + '\'' +
                ", rom='" + rom + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", isDeleted=" + isDeleted +
                '}';
    }
}