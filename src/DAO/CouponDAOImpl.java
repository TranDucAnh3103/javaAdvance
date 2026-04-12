package DAO;

import Models.MarketingPromotions.Coupon;
import Util.ConnectDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CouponDAOImpl implements CouponDAO {

    @Override
    public boolean createCoupon(Coupon coupon) throws Exception {
        String sql = "INSERT INTO coupons (code, discount_percent, valid_from, valid_to, usage_limit, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, coupon.getCode());
            ps.setBigDecimal(2, coupon.getDiscountPercent());
            ps.setTimestamp(3, coupon.getValidFrom());
            ps.setTimestamp(4, coupon.getValidTo());
            ps.setInt(5, coupon.getUsageLimit());
            ps.setBoolean(6, coupon.isActive());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Coupon> getAllCoupons() throws Exception {
        List<Coupon> list = new ArrayList<>();
        String sql = "SELECT * FROM coupons ORDER BY valid_from DESC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCoupon(rs));
            }
        }
        return list;
    }

    /**
     * Tìm coupon theo mã code.
     * Hàm chỉ tìm và trả về — KHÔNG kiểm tra hạn dùng hay usage_limit.
     * Việc validate "coupon còn dùng được không?" thuộc tầng Service (business logic).
     */
    @Override
    public Coupon getCouponByCode(String code) throws Exception {
        String sql = "SELECT * FROM coupons WHERE code = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCoupon(rs);
                }
            }
        }
        return null;
    }

    /**
     * Trừ 1 lượt sử dụng.
     * Mệnh đề WHERE usage_limit > 0 đảm bảo nếu 2 người cùng nhập mã lúc cuối cùng,
     * chỉ 1 người được dùng (người còn lại: 0 rows affected → Exception ở tầng Service).
     */
    @Override
    public boolean deductUsage(int couponId) throws Exception {
        String sql = "UPDATE coupons SET usage_limit = usage_limit - 1 WHERE id = ? AND usage_limit > 0";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, couponId);
            return ps.executeUpdate() > 0;
        }
    }

    // ===== Helper =====
    private Coupon mapCoupon(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setDiscountPercent(rs.getBigDecimal("discount_percent"));
        c.setValidFrom(rs.getTimestamp("valid_from"));
        c.setValidTo(rs.getTimestamp("valid_to"));
        c.setUsageLimit(rs.getInt("usage_limit"));
        c.setActive(rs.getBoolean("is_active"));
        return c;
    }
}
