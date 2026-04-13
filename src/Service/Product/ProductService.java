package Service.Product;

import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;
import Exceptions.ProductNotFoundException;
import Models.DTO.ProductDTO;
import Models.CoreEntities.Product;
import Models.CoreEntities.ProductVariant;

import java.util.List;

public interface ProductService {
    /**
     * Lấy danh sách sản phẩm với phân trang, search và sort
     * @param inStockOnly True: dành cho Khách hàng (Chỉ lấy tồn kho > 0). False: dành cho Admin (Lấy tất cả).
     */
    List<ProductDTO> getAllProducts(int limit, int offset, String searchQuery, String sortBy, String sortOrder, boolean inStockOnly) throws DatabaseException;

    /**
     * Lấy tổng sản phẩm thỏa mãn điều kiện search và inStock
     */
    int getTotalProducts(String searchQuery, boolean inStockOnly) throws DatabaseException;

    /**
     * Thêm sản phẩm mới với validate
     */
    boolean addProduct(Product product, ProductVariant variant) throws InvalidInputException, DatabaseException;

    /**
     * Tìm sản phẩm để hiển thị trước khi update
     */
    Product getProductById(int productId) throws ProductNotFoundException, DatabaseException;

    /**
     * Cập nhật thông tin sản phẩm
     */
    boolean updateProduct(Product product, ProductVariant variant) throws InvalidInputException, ProductNotFoundException, DatabaseException;

    /**
     * Xóa sản phẩm
     */
    boolean deleteProduct(int productId) throws ProductNotFoundException, DatabaseException;
}
