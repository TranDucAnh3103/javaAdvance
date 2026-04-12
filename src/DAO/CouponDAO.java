package DAO;

import Models.MarketingPromotions.Coupon;

import java.util.List;

/**
 * Interface thao tác CSDL cho Mã giảm giá (Coupon).
 * Admin tạo coupon → Customer nhập mã lúc checkout → hệ thống trừ usage_limit.
 */
public interface CouponDAO {

    /** Admin: Tạo mã giảm giá mới. */
    boolean createCoupon(Coupon coupon) throws Exception;

    /** Admin: Xem toàn bộ mã giảm giá trong hệ thống. */
    List<Coupon> getAllCoupons() throws Exception;

    /**
     * Tìm coupon theo mã code — Customer sẽ nhập mã này lúc checkout.
     * Trả null nếu mã không tồn tại.
     */
    Coupon getCouponByCode(String code) throws Exception;

    /**
     * Trừ 1 lượt sử dụng sau khi Customer checkout thành công.
     * WHERE usage_limit > 0 là lưới an toàn chống trừ quá mức (giống logic chống Over-selling).
     */
    boolean deductUsage(int couponId) throws Exception;
}
