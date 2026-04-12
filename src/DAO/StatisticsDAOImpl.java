package DAO;

import Models.DTO.TopProductDTO;
import Util.ConnectDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatisticsDAOImpl implements StatisticsDAO {

    /**
     * Truy vấn Top N sản phẩm bán chạy nhất tháng.
     *
     * Giải thích câu SQL:
     *   - JOIN 3 bảng: order_details → product_variants → products (để lấy tên SP gốc)
     *   - JOIN thêm orders để lọc theo tháng/năm và loại đơn bị hủy
     *   - GROUP BY p.id vì 1 product có nhiều variant, ta muốn gộp doanh số tất cả variant lại
     *   - ORDER BY total_sold DESC → SP bán nhiều nhất lên đầu
     *   - LIMIT ? → chỉ lấy Top N (mặc định 5)
     *
     * Tại sao loại CANCELLED?
     *   Vì đơn hủy thì hàng đã bị hoàn kho, không nên tính vào "bán chạy".
     */
    @Override
    public List<TopProductDTO> getTopSellingProducts(int month, int year, int limit) throws Exception {
        List<TopProductDTO> list = new ArrayList<>();
        String sql = """
            SELECT p.name AS product_name,
                   SUM(od.quantity) AS total_sold,
                   SUM(od.quantity * od.unit_price) AS revenue
            FROM order_details od
            JOIN product_variants pv ON od.variant_id = pv.id
            JOIN products p ON pv.product_id = p.id
            JOIN orders o ON od.order_id = o.id
            WHERE MONTH(o.created_at) = ? AND YEAR(o.created_at) = ?
              AND o.status != 'CANCELLED'
            GROUP BY p.id, p.name
            ORDER BY total_sold DESC
            LIMIT ?
            """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopProductDTO dto = new TopProductDTO();
                    dto.setProductName(rs.getString("product_name"));
                    dto.setTotalSold(rs.getInt("total_sold"));
                    dto.setRevenue(rs.getBigDecimal("revenue"));
                    list.add(dto);
                }
            }
        }
        return list;
    }
}
