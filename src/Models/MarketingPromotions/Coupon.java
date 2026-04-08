package Models.MarketingPromotions;

import java.math.BigDecimal; // Xử lý số thực độ chính xác cao, chống sai số khi tính toán tiền tệ.
import java.sql.Timestamp; // Thư viện mở rộng từ java.util.Date, tương thích hoàn toàn với kiểu TIMESTAMP của SQL.

public class Coupon {
    private int id;
    private String code;
    private BigDecimal discountPercent;
    private Timestamp validFrom;
    private Timestamp validTo;
    private int usageLimit;
    private boolean isActive;

    public Coupon() {}

    public Coupon(int id, String code, BigDecimal discountPercent, Timestamp validFrom,
                  Timestamp validTo, int usageLimit, boolean isActive) {
        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.usageLimit = usageLimit;
        this.isActive = isActive;
    }

    /**
     * Logic kiểm tra Coupon có khả dụng hay không (Dùng cho tầng Service)
     * @return true nếu thỏa mãn mọi điều kiện: active, còn lượt dùng, trong thời hạn.
     */
    public boolean isAvailable() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return isActive &&
                usageLimit > 0 &&
                now.after(validFrom) &&
                now.before(validTo);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(int usageLimit) {
        this.usageLimit = usageLimit;
    }

    public Timestamp getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Timestamp validFrom) {
        this.validFrom = validFrom;
    }

    public Timestamp getValidTo() {
        return validTo;
    }

    public void setValidTo(Timestamp validTo) {
        this.validTo = validTo;
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "code='" + code + '\'' +
                ", id=" + id +
                ", discountPercent=" + discountPercent +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", usageLimit=" + usageLimit +
                ", isActive=" + isActive +
                '}';
    }
}