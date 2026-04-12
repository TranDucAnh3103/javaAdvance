package Service;

import DAO.CouponDAO;
import DAO.CouponDAOImpl;
import Models.MarketingPromotions.Coupon;
import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CouponServiceImpl implements CouponService {

    private CouponDAO couponDAO = new CouponDAOImpl();

    /**
     * Tạo coupon mới.
     * Validate: Code không trống, % giảm trong (0, 100], thời gian hợp lệ, lượt dùng > 0.
     */
    @Override
    public boolean createCoupon(Coupon coupon) throws InvalidInputException, DatabaseException {
        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            throw new InvalidInputException("Mã coupon không được để trống!");
        }
        if (coupon.getDiscountPercent() == null ||
                coupon.getDiscountPercent().compareTo(BigDecimal.ZERO) <= 0 ||
                coupon.getDiscountPercent().compareTo(new BigDecimal("100")) > 0) {
            throw new InvalidInputException("Phần trăm giảm giá phải nằm trong khoảng (0% - 100%]!");
        }
        if (coupon.getValidFrom() == null || coupon.getValidTo() == null) {
            throw new InvalidInputException("Thời gian hiệu lực không được để trống!");
        }
        if (!coupon.getValidFrom().before(coupon.getValidTo())) {
            throw new InvalidInputException("Ngày bắt đầu phải TRƯỚC ngày kết thúc!");
        }
        if (coupon.getUsageLimit() <= 0) {
            throw new InvalidInputException("Số lượt sử dụng phải > 0!");
        }

        try {
            return couponDAO.createCoupon(coupon);
        } catch (Exception e) {
            // Bắt lỗi trùng mã code (UNIQUE constraint) → thông báo rõ ràng
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                throw new InvalidInputException("Mã coupon '" + coupon.getCode() + "' đã tồn tại!");
            }
            throw new DatabaseException("Lỗi khi tạo coupon: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Coupon> getAllCoupons() throws DatabaseException {
        try {
            return couponDAO.getAllCoupons();
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi lấy danh sách coupon: " + e.getMessage(), e);
        }
    }

    /**
     * Customer nhập mã giảm giá → ta phải kiểm tra đủ 4 điều kiện:
     *   1. Mã code có tồn tại trong DB không?
     *   2. Coupon còn active không (Admin có thể tắt bất cứ lúc nào)?
     *   3. Thời điểm hiện tại có nằm trong khoảng valid_from → valid_to không?
     *   4. usage_limit còn > 0 không (hết lượt thì hết khuyến mãi)?
     * Nếu thiếu 1 trong 4 → ném Exception với lý do cụ thể.
     */
    @Override
    public Coupon validateAndGetCoupon(String code) throws InvalidInputException, DatabaseException {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidInputException("Mã coupon không được để trống!");
        }

        try {
            Coupon coupon = couponDAO.getCouponByCode(code.trim().toUpperCase());
            if (coupon == null) {
                throw new InvalidInputException("Mã giảm giá '" + code + "' không tồn tại trong hệ thống!");
            }

            // Dùng method isAvailable() đã có sẵn trong Model Coupon.java
            // Method này check cả 3 điều kiện: active, còn lượt, trong thời hạn
            if (!coupon.isAvailable()) {
                // Phân tích chi tiết lý do để Customer biết vấn đề nằm ở đâu
                if (!coupon.isActive()) {
                    throw new InvalidInputException("Mã '" + code + "' đã bị vô hiệu hóa bởi Admin.");
                }
                if (coupon.getUsageLimit() <= 0) {
                    throw new InvalidInputException("Mã '" + code + "' đã hết lượt sử dụng.");
                }
                throw new InvalidInputException("Mã '" + code + "' đã hết hạn hoặc chưa đến thời gian áp dụng.");
            }

            return coupon;
        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Lỗi kiểm tra mã giảm giá: " + e.getMessage(), e);
        }
    }

    /**
     * Tính tiền sau giảm.
     * VD: Tổng = 50,000,000, coupon giảm 10% → 50,000,000 × 0.9 = 45,000,000 VNĐ.
     */
    @Override
    public BigDecimal applyCouponDiscount(BigDecimal totalAmount, Coupon coupon) {
        BigDecimal multiplier = BigDecimal.valueOf(100).subtract(coupon.getDiscountPercent())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return totalAmount.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean deductUsage(int couponId) throws DatabaseException {
        try {
            return couponDAO.deductUsage(couponId);
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi trừ lượt sử dụng coupon: " + e.getMessage(), e);
        }
    }
}
