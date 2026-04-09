package Service;

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
     */
    List<ProductDTO> getAllProducts(int limit, int offset, String searchQuery, String sortBy, String sortOrder) throws DatabaseException;

    /**
     * Lấy tổng sản phẩm thỏa mãn điều kiện search
     */
    int getTotalProducts(String searchQuery) throws DatabaseException;

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
