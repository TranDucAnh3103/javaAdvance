package DAO;

import Models.MarketingPromotions.FlashSale;
import Models.MarketingPromotions.FlashSaleItem;
import Util.ConnectDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Triển khai thao tác CSDL cho Flash Sale.
 * Hai bảng chính: flash_sales (khung sự kiện) + flash_sale_items (chi tiết SP tham gia).
 */
public class FlashSaleDAOImpl implements FlashSaleDAO {

    /**
     * Tạo sự kiện Flash Sale mới.
     * Trả về ID tự sinh — Admin cần ID này để tiếp tục thêm SP vào sự kiện.
     */
    @Override
    public int createFlashSale(FlashSale flashSale) throws Exception {
        String sql = "INSERT INTO flash_sales (name, start_time, end_time, is_active) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, flashSale.getName());
            ps.setTimestamp(2, flashSale.getStartTime());
            ps.setTimestamp(3, flashSale.getEndTime());
            ps.setBoolean(4, flashSale.isActive());

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Tạo Flash Sale thất bại, không row nào được insert.");

            // Lấy ID tự sinh để trả về — giống cách lấy Order ID trong OrderDAOImpl
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                else throw new SQLException("Tạo Flash Sale OK nhưng không lấy được ID tự sinh.");
            }
        }
    }

    /**
     * Gắn 1 variant vào Flash Sale (Admin chọn SP, nhập % giảm và số lượng dành cho Flash).
     * Khóa chính là composite (flash_sale_id, variant_id) → 1 variant chỉ tham gia 1 lần trong 1 sự kiện.
     */
    @Override
    public boolean addFlashSaleItem(FlashSaleItem item) throws Exception {
        String sql = "INSERT INTO flash_sale_items (flash_sale_id, variant_id, discount_percent, sale_stock) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, item.getFlashSaleId());
            ps.setInt(2, item.getVariantId());
            ps.setBigDecimal(3, item.getDiscountPercent());
            ps.setInt(4, item.getSaleStock());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Kéo danh sách Flash Sale đang diễn ra — NOW() phải nằm giữa start_time và end_time.
     * Chỉ lấy những sự kiện is_active = TRUE (Admin có thể tắt sự kiện sớm nếu muốn).
     */
    @Override
    public List<FlashSale> getActiveFlashSales() throws Exception {
        List<FlashSale> list = new ArrayList<>();
        String sql = "SELECT * FROM flash_sales WHERE is_active = TRUE AND NOW() BETWEEN start_time AND end_time";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapFlashSale(rs));
            }
        }
        return list;
    }

    @Override
    public List<FlashSale> getAllFlashSales() throws Exception {
        List<FlashSale> list = new ArrayList<>();
        String sql = "SELECT * FROM flash_sales ORDER BY start_time DESC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapFlashSale(rs));
            }
        }
        return list;
    }

    @Override
    public List<FlashSaleItem> getFlashSaleItems(int flashSaleId) throws Exception {
        List<FlashSaleItem> list = new ArrayList<>();
        String sql = "SELECT * FROM flash_sale_items WHERE flash_sale_id = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, flashSaleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapFlashSaleItem(rs));
                }
            }
        }
        return list;
    }

    /**
     * Kiểm tra 1 variant có đang tham gia Flash Sale nào đang diễn ra không.
     * Điều kiện kiểm tra khắt khe:
     *   1. Sự kiện phải đang active (is_active = TRUE)
     *   2. Thời điểm hiện tại phải nằm trong khung giờ sự kiện
     *   3. sale_stock phải còn > 0 (hết hàng flash thì coi như hết khuyến mãi)
     * Nếu không thỏa 1 trong 3 → trả null → Customer mua giá thường.
     */
    @Override
    public FlashSaleItem getActiveFlashSaleItem(int variantId) throws Exception {
        String sql = """
            SELECT fsi.* FROM flash_sale_items fsi
            JOIN flash_sales fs ON fsi.flash_sale_id = fs.id
            WHERE fsi.variant_id = ?
              AND fs.is_active = TRUE
              AND NOW() BETWEEN fs.start_time AND fs.end_time
              AND fsi.sale_stock > 0
            LIMIT 1
            """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapFlashSaleItem(rs);
                }
            }
        }
        return null; // Variant này không tham gia Flash Sale nào hiện tại
    }

    /**
     * Trừ sale_stock khi Customer checkout hàng Flash Sale.
     * WHERE sale_stock >= ? là lưới chống bán vượt — y hệt logic chống Over-selling của stock chính.
     * Nếu 2 người cùng mua hàng Flash cuối cùng, ai nhanh hơn thì ăn.
     */
    @Override
    public boolean deductSaleStock(int flashSaleId, int variantId, int quantity) throws Exception {
        String sql = "UPDATE flash_sale_items SET sale_stock = sale_stock - ? WHERE flash_sale_id = ? AND variant_id = ? AND sale_stock >= ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, flashSaleId);
            ps.setInt(3, variantId);
            ps.setInt(4, quantity); // Mệnh đề WHERE kiểm tra sale_stock >= quantity
            return ps.executeUpdate() > 0;
        }
    }

    // ===== Helper: Map ResultSet → Object =====

    private FlashSale mapFlashSale(ResultSet rs) throws SQLException {
        FlashSale fs = new FlashSale();
        fs.setId(rs.getInt("id"));
        fs.setName(rs.getString("name"));
        fs.setStartTime(rs.getTimestamp("start_time"));
        fs.setEndTime(rs.getTimestamp("end_time"));
        fs.setActive(rs.getBoolean("is_active"));
        return fs;
    }

    private FlashSaleItem mapFlashSaleItem(ResultSet rs) throws SQLException {
        FlashSaleItem item = new FlashSaleItem();
        item.setFlashSaleId(rs.getInt("flash_sale_id"));
        item.setVariantId(rs.getInt("variant_id"));
        item.setDiscountPercent(rs.getBigDecimal("discount_percent"));
        item.setSaleStock(rs.getInt("sale_stock"));
        return item;
    }
}
