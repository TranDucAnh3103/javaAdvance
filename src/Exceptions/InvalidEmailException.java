package Exceptions;

/**
 * Custom Exception dùng để xử lý các lỗi định dạng của Email.
 * Thường được sử dụng khi người dùng nhập sai quy tắc (ví dụ thiếu @ hoặc tên miền).
 */
public class InvalidEmailException extends Exception {
    public InvalidEmailException(String message) {
        super(message);
    }
}
