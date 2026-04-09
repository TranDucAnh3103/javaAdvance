package DAO;

import Models.DTO.ProductDTO;

import java.util.List;

public interface ProductDAO {
    /**
     * Lấy danh sách sản phẩm hiển thị trên giao diện (hỗ trợ phân trang, sắp xếp và tìm kiếm).
     * @param limit Số lượng bản ghi tối đa
     * @param offset Điểm bắt đầu
     * @param searchQuery Từ khóa tìm kiếm (null nếu không tìm kiếm)
     * @param sortBy Cột cần sắp xếp (ví dụ: "price")
     * @param sortOrder Hướng sắp xếp ("ASC" hoặc "DESC")
     */
    List<ProductDTO> getAllProducts(int limit, int offset, String searchQuery, String sortBy, String sortOrder) throws Exception;

    /**
     * Lấy tổng số lượng sản phẩm để tính total pages.
     */
    int getTotalProducts(String searchQuery) throws Exception;

    /**
     * Chèn Product và Variant trong cùng 1 transaction (Atomic).
     */
    boolean insertProductWithVariant(Models.CoreEntities.Product product, Models.CoreEntities.ProductVariant variant) throws Exception;

    /**
     * Tìm sản phẩm theo ID
     */
    java.util.Optional<Models.CoreEntities.Product> getProductById(int productId) throws Exception;

    /**
     * Cập nhật Product và Variant
     */
    boolean updateProductWithVariant(Models.CoreEntities.Product product, Models.CoreEntities.ProductVariant variant) throws Exception;

    /**
     * Xóa mềm sản phẩm (và cả biến thể)
     */
    boolean softDeleteProduct(int productId) throws Exception;
}
