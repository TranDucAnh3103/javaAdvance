package Models.MarketingPromotions;

import java.math.BigDecimal; // Xử lý số thực độ chính xác cao, chống sai số khi tính toán tiền tệ.

public class FlashSaleItem {
    private int flashSaleId;
    private int variantId;
    private BigDecimal discountPercent;
    private int saleStock; // Kho riêng biệt cho Flash Sale

    public FlashSaleItem() {}

    public FlashSaleItem(int flashSaleId, int variantId, BigDecimal discountPercent, int saleStock) {
        this.flashSaleId = flashSaleId;
        this.variantId = variantId;
        this.discountPercent = discountPercent;
        this.saleStock = saleStock;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getFlashSaleId() {
        return flashSaleId;
    }

    public void setFlashSaleId(int flashSaleId) {
        this.flashSaleId = flashSaleId;
    }

    public int getSaleStock() {
        return saleStock;
    }

    public void setSaleStock(int saleStock) {
        this.saleStock = saleStock;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    @Override
    public String toString() {
        return "FlashSaleItem{" +
                "discountPercent=" + discountPercent +
                ", flashSaleId=" + flashSaleId +
                ", variantId=" + variantId +
                ", saleStock=" + saleStock +
                '}';
    }
}