package Exceptions;

/**
 * Custom Exception dùng để xử lý các lỗi liên quan đến sản phẩm
 * Thường được ném ra ở tầng Service khi không tìm thấy sản phẩm
 */
public class ProductNotFoundException extends Exception {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
