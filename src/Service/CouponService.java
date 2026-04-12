package Service;

import Models.MarketingPromotions.Coupon;
import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    /** Admin: Tạo mã giảm giá mới. */
    boolean createCoupon(Coupon coupon) throws InvalidInputException, DatabaseException;

    /** Admin: Xem toàn bộ mã giảm giá. */
    List<Coupon> getAllCoupons() throws DatabaseException;

    /**
     * Customer: Nhập mã giảm giá → validate → trả về Coupon nếu hợp lệ.
     * Ném Exception nếu: mã không tồn tại, hết hạn, hết lượt, bị tắt.
     */
    Coupon validateAndGetCoupon(String code) throws InvalidInputException, DatabaseException;

    /**
     * Tính tổng tiền sau khi áp dụng coupon.
     * Công thức: totalAmount × (1 - discount_percent / 100)
     */
    BigDecimal applyCouponDiscount(BigDecimal totalAmount, Coupon coupon);

    /** Trừ 1 lượt sử dụng sau khi checkout thành công. */
    boolean deductUsage(int couponId) throws DatabaseException;
}
