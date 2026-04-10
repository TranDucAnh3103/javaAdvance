package DAO;

import Models.DTO.ProductDTO;
import Models.CoreEntities.Product;
import Models.CoreEntities.ProductVariant;
import Util.ConnectDB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAOImpl implements ProductDAO {

    /**
     *  Giải thích thuật toán Lọc & Tìm kiếm (Pagination & Search)
     * - Tại Database: Lệnh `LIKE` tốn thời gian O(N) vì quét qua chuỗi, nhưng `ORDER BY` mất O(N log N).
     * - Vì thao tác lọc được làm bằng SQL, nên thay vì tải hàng triệu dòng lên RAM (Java) gây OutOfMemory, 
     *   ta chỉ bắt DB trả đúng số bản ghi với LIMIT/OFFSET -> O(K) Space Complexity (K là limit).
     */
    @Override
    public List<ProductDTO> getAllProducts(int limit, int offset, String searchQuery, String sortBy, String sortOrder, boolean inStockOnly) throws Exception {
        List<ProductDTO> list = new ArrayList<>();

        // Sử dụng StringBuilder để thoải mái ghép chuỗi SQL động (Dynamic SQL).
        StringBuilder sql = new StringBuilder("""
            SELECT 
                c.name AS category_name,
                p.id AS product_id,
                p.name AS product_name,
                p.brand,
                pv.id AS variant_id,
                pv.ram,
                pv.rom,
                pv.color,
                pv.price,
                pv.stock
            FROM categories c
            JOIN products p ON c.id = p.category_id
            JOIN product_variants pv ON p.id = pv.product_id
            WHERE c.is_deleted = FALSE 
              AND p.is_deleted = FALSE 
              AND pv.is_deleted = FALSE
        """);

        // [EDGE CASE]: Nếu người dùng có nhập từ khóa, ta ghép toán tử LIKE vào.
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append(" AND p.name LIKE ? ");
        }
        
        // Cờ Lọc Kho Hàng cho Khách (Chỉ hiển thị đồ có lượng tồn kho > 0)
        if (inStockOnly) {
            sql.append(" AND pv.stock > 0 ");
        }

        // Kiểm tra chiều sắp xếp động
        if (sortBy != null && !sortBy.isEmpty()) {
            if ("price".equalsIgnoreCase(sortBy)) {
                sql.append(" ORDER BY pv.price ").append("DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC");
            } else {
                sql.append(" ORDER BY p.id, pv.id ");
            }
        } else {
            sql.append(" ORDER BY p.id, pv.id "); // Mặc định luôn sort theo ID để chống xáo trộn dữ liệu
        }
        
        sql.append(" LIMIT ? OFFSET ?;");

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            
            // Set params linh hoạt vì số lượng tham số dấu chấm hỏi (?) thay đổi dựa trên việc có Search hay ko
            int paramIndex = 1;
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                ps.setString(paramIndex++, "%" + searchQuery.trim() + "%");
            }
            ps.setInt(paramIndex++, limit);
            ps.setInt(paramIndex, offset);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()){
                    ProductDTO dto = new ProductDTO();
                    dto.setCategoryName(rs.getString("category_name"));
                    dto.setProductId(rs.getInt("product_id"));
                    dto.setProductName(rs.getString("product_name"));
                    dto.setBrand(rs.getString("brand"));
                    dto.setVariantId(rs.getInt("variant_id"));
                    dto.setRam(rs.getString("ram"));
                    dto.setRom(rs.getString("rom"));
                    dto.setColor(rs.getString("color"));
                    dto.setPrice(BigDecimal.valueOf(rs.getDouble("price")));
                    dto.setStock(rs.getInt("stock"));
                    list.add(dto);
                }
            }
        }
        return list;
    }

    @Override
    public int getTotalProducts(String searchQuery, boolean inStockOnly) throws Exception {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*) AS total
            FROM categories c
            JOIN products p ON c.id = p.category_id
            JOIN product_variants pv ON p.id = pv.product_id
            WHERE c.is_deleted = FALSE 
              AND p.is_deleted = FALSE 
              AND pv.is_deleted = FALSE
        """);

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append(" AND p.name LIKE ? ");
        }
        
        if (inStockOnly) {
            sql.append(" AND pv.stock > 0 ");
        }

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                ps.setString(1, "%" + searchQuery.trim() + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    /**
     * Logic Transaction Data Flow
     * Vì yêu cầu lưu dữ liệu vào 2 bảng khác nhau nên Database Transaction được kích hoạt (setAutoCommit(false)).
     * Nếu 1 trong 2 lệnh lưu bảng thất bại -> Gọi `con.rollback()` xoá sạch dữ liệu rác trước đó.
     */
    @Override
    public boolean insertProductWithVariant(Product product, ProductVariant variant) throws Exception {
        String sqlProduct = "INSERT INTO products (category_id, name, brand, description, is_deleted) VALUES (?, ?, ?, ?, false)";
        String sqlVariant = "INSERT INTO product_variants (product_id, ram, rom, color, price, stock, is_deleted) VALUES (?, ?, ?, ?, ?, ?, false)";
        
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            // Tắt auto commit để gộp 2 truy vấn dính liền vào 1 block an toàn
            con.setAutoCommit(false); 

            // CÚ PHÁP LẤY GENERATED ID: Khai báo Statement.RETURN_GENERATED_KEYS
            try (PreparedStatement psProduct = con.prepareStatement(sqlProduct, Statement.RETURN_GENERATED_KEYS)) {
                psProduct.setInt(1, product.getCategoryId());
                psProduct.setString(2, product.getName());
                psProduct.setString(3, product.getBrand());
                psProduct.setString(4, product.getDescription());
                
                int affectedRows = psProduct.executeUpdate();
                if (affectedRows == 0) {
                    con.rollback();
                    return false;
                }

                // Hút Auto Increment ID từ MySQL trả về vào ResultSet
                try (ResultSet generatedKeys = psProduct.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newProductId = generatedKeys.getInt(1);
                        variant.setProductId(newProductId); // Trói ID cha vào Variant Con
                    } else {
                        con.rollback();
                        return false; 
                    }
                }
            }

            // Bắt đầu Insert cho bảng con (Variant) khi ĐÃ CHẮC CHẮN MANG TRONG MÌNH ID CỦA PRODUCT CHA
            try (PreparedStatement psVariant = con.prepareStatement(sqlVariant)) {
                psVariant.setInt(1, variant.getProductId());
                psVariant.setString(2, variant.getRam());
                psVariant.setString(3, variant.getRom());
                psVariant.setString(4, variant.getColor());
                psVariant.setBigDecimal(5, variant.getPrice());
                psVariant.setInt(6, variant.getStock());
                
                psVariant.executeUpdate();
            }

            con.commit(); // Khai pháo 2 lệnh thành công! Lưu chung Data vào HDD.
            return true;

        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback(); // [EDGE CASE] Nếu có bất kỳ Exception gì (mất mạng, db đứng), Reset Data ngay!
                } catch (SQLException ex) {
                    throw new Exception("Lỗi rollback: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    /**
     * Mẫu thiết kế Optional
     * Thay vì trả về `null` rất dễ gây lỗi `NullPointerException`
     * Việc trả về Optional giúp dễ bảo trì
     */
    @Override
    public Optional<Product> getProductById(int productId) throws Exception {
        String sql = "SELECT * FROM products WHERE id = ? AND is_deleted = false";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setCategoryId(rs.getInt("category_id"));
                    p.setName(rs.getString("name"));
                    p.setBrand(rs.getString("brand"));
                    p.setDescription(rs.getString("description"));
                    p.setDeleted(rs.getBoolean("is_deleted"));
                    return Optional.of(p); // Phát sinh đối tượng Optional bọc Product
                }
            }
        }
        return Optional.empty(); // Rỗng! Không có đối tượng
    }

    @Override
    public boolean updateProductWithVariant(Product product, ProductVariant variant) throws Exception {
        String sqlProduct = "UPDATE products SET category_id = ?, name = ?, brand = ?, description = ? WHERE id = ? AND is_deleted = false";
        String sqlVariant = "UPDATE product_variants SET ram = ?, rom = ?, color = ?, price = ?, stock = ? WHERE product_id = ? AND id = ? AND is_deleted = false";
        
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false); 

            try (PreparedStatement psProduct = con.prepareStatement(sqlProduct)) {
                psProduct.setInt(1, product.getCategoryId());
                psProduct.setString(2, product.getName());
                psProduct.setString(3, product.getBrand());
                psProduct.setString(4, product.getDescription());
                psProduct.setInt(5, product.getId());
                psProduct.executeUpdate();
            }

            try (PreparedStatement psVariant = con.prepareStatement(sqlVariant)) {
                psVariant.setString(1, variant.getRam());
                psVariant.setString(2, variant.getRom());
                psVariant.setString(3, variant.getColor());
                psVariant.setBigDecimal(4, variant.getPrice());
                psVariant.setInt(5, variant.getStock());
                psVariant.setInt(6, product.getId());
                psVariant.setInt(7, variant.getId());
                psVariant.executeUpdate();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    throw new Exception("Lỗi rollback: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    /**
     * Chế độ Soft Delete (Xóa mềm)
     * Tại sao lại UPDATE chứ ko dùng lệnh DELETE?
     * -> Vì nếu xóa hẳn sẽ dẫn đến báo lỗi khóa ngoại nếu người dùng đã có Order mua Product đó trước đây.
     * Hủy hoạt động chỉ bằng cách chuyển `is_deleted = true`.
     */
    @Override
    public boolean softDeleteProduct(int productId) throws Exception {
        String sqlProduct = "UPDATE products SET is_deleted = true WHERE id = ?";
        String sqlVariant = "UPDATE product_variants SET is_deleted = true WHERE product_id = ?";
        
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement psProduct = con.prepareStatement(sqlProduct)) {
                psProduct.setInt(1, productId);
                psProduct.executeUpdate();
            }

            try (PreparedStatement psVariant = con.prepareStatement(sqlVariant)) {
                psVariant.setInt(1, productId);
                psVariant.executeUpdate();
            }

            con.commit(); // Thành công ẩn đồng loạt sản phẩm lẫn biến thể.
            return true;
        } catch (Exception e) {
            if (con != null) {
                con.rollback();
            }
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }
}
