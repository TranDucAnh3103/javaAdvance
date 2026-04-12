package Models.DTO;

import java.math.BigDecimal;

/**
 * DTO dùng cho báo cáo thống kê Top sản phẩm bán chạy.
 * Chứa thông tin tổng hợp từ nhiều bảng (products, order_details, orders) — không map 1:1 với bảng nào.
 */
public class TopProductDTO {
    private String productName;
    private int totalSold;       // Tổng số lượng bán được (SUM của quantity)
    private BigDecimal revenue;  // Tổng doanh thu (SUM của quantity × unit_price)

    public TopProductDTO() {}

    public TopProductDTO(String productName, int totalSold, BigDecimal revenue) {
        this.productName = productName;
        this.totalSold = totalSold;
        this.revenue = revenue;
    }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getTotalSold() { return totalSold; }
    public void setTotalSold(int totalSold) { this.totalSold = totalSold; }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
}
