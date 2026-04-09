package Exceptions;

/**
 * Custom Exception tổng quát cho các lỗi nhập liệu.
 * Dùng khi các trường đầu vào bắt buộc bị bỏ trống, hoặc chứa các ký tự không được phép tĩnh.
 */
public class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}
