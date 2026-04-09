package Exceptions;

/**
 * Custom Exception dùng để xử lý các lỗi liên quan đến định dạng Số Điện Thoại.
 * Thường được ném ra ở tầng Service khi đầu vào không khớp regex quy định.
 */
public class InvalidPhoneNumberException extends Exception {
    public InvalidPhoneNumberException(String message) {
        super(message);
    }
}
