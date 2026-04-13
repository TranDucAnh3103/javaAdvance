package Service.Product;

import DAO.Product.ProductDAO;
import DAO.Product.ProductDAOImpl;
import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;
import Exceptions.ProductNotFoundException;
import Models.DTO.ProductDTO;
import Models.CoreEntities.Product;
import Models.CoreEntities.ProductVariant;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {
    
    // SOLID Principles - L (Liskov) & D (Dependency Inversion)
    // Tận dụng tính đa hình: Kiểu khai báo là Interface (ProductDAO), Khởi tạo là class (ProductDAOImpl)
    // Bất kỳ lớp nào cài đặt ProductDAO cũng có thể được gắn qua biến prodao này.
    private ProductDAO prodao = new ProductDAOImpl();

    @Override
    public List<ProductDTO> getAllProducts(int limit, int offset, String searchQuery, String sortBy, String sortOrder, boolean inStockOnly) throws DatabaseException {
        try {
            return prodao.getAllProducts(limit, offset, searchQuery, sortBy, sortOrder, inStockOnly);
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi lấy danh sách sản phẩm: " + e.getMessage(), e);
        }
    }

    @Override
    public int getTotalProducts(String searchQuery, boolean inStockOnly) throws DatabaseException {
        try {
            return prodao.getTotalProducts(searchQuery, inStockOnly);
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi lấy tổng số sản phẩm: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean addProduct(Product product, ProductVariant variant) throws InvalidInputException, DatabaseException {
        // Validation: Đẩy logic kiểm tra xuống Service thay vì viết lặp ở Presentation.
        validateProduct(product, variant);

        try {
            return prodao.insertProductWithVariant(product, variant);
        } catch (Exception e) {
            throw new DatabaseException("Lỗi hệ thống khi thêm sản phẩm: " + e.getMessage(), e);
        }
    }

    @Override
    public Product getProductById(int productId) throws ProductNotFoundException, DatabaseException {
        try {
            // Dùng Optional thay cho Null Check thuần.
            // Lợi ích: Dòng code ngắn gọn, rõ nghĩa, và quẳng Exception ngay tức khắc bằng Lambda Expression.
            Optional<Product> prodOpt = prodao.getProductById(productId);
            return prodOpt.orElseThrow(() -> new ProductNotFoundException("Không tìm thấy sản phẩm có ID: " + productId));
        } catch (ProductNotFoundException ex) {
            throw ex; // [EDGE CASE] Nếu là lỗi Not Found, ta giữ nguyên ko biến tính để đẩy ra UI
        } catch (Exception e) {
            throw new DatabaseException("Lỗi cơ sở dữ liệu khi tìm kiếm sản phẩm: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateProduct(Product product, ProductVariant variant) throws InvalidInputException, ProductNotFoundException, DatabaseException {
        validateProduct(product, variant);

        try {
            // [EDGE CASE] Kiểm tra xem sản phẩm có tồn tại trước khi cập nhật.
            // Vì Update và Delete nếu truyền vào ID sai thì vẫn báo Success ngầm nếu không kiểm tra do DB không có lỗi lỗi constraint.
            // Dòng code này sẽ ném ra ProductNotFoundException nếu tìm không thấy.
            getProductById(product.getId()); 

            return prodao.updateProductWithVariant(product, variant);
        } catch (ProductNotFoundException | InvalidInputException ex) {
            throw ex;
        } catch (Exception e) {
            throw new DatabaseException("Lỗi hệ thống khi cập nhật sản phẩm: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteProduct(int productId) throws ProductNotFoundException, DatabaseException {
        try {
            // [EDGE CASE] Hệt như update, phải chắn Null đầu vào từ ID dỏm
            getProductById(productId); 
            return prodao.softDeleteProduct(productId);
        } catch (ProductNotFoundException ex) {
            throw ex;
        } catch (Exception e) {
            throw new DatabaseException("Lỗi hệ thống khi xóa sản phẩm: " + e.getMessage(), e);
        }
    }

    /**
     * Hàm helper private phục vụ quy tắc Clean Code "DRY" (Don't Repeat Yourself).
     * Hàm này được tái sử dụng đồng thời bởi Add và Update form thay vì copy & paste khối lệnh if.
     */
    private void validateProduct(Product product, ProductVariant variant) throws InvalidInputException {
        // Ngăn chắn các trường hợp biên ngớ ngẩn (Edge cases rỗng Object).
        if (product == null || variant == null) {
            throw new InvalidInputException("Dữ liệu sản phẩm không được null.");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new InvalidInputException("Tên sản phẩm không hợp lệ hoặc để trống!");
        }
        if (variant.getPrice() == null || variant.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException("Giá sản phẩm phải lớn hơn 0.");
        }
        if (variant.getStock() < 0) {
            throw new InvalidInputException("Số lượng trong kho không được âm.");
        }
    }
}
