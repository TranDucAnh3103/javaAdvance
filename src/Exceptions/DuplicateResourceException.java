package Exceptions;

/**
 * Custom Exception để thông báo lỗi trùng lặp dữ liệu (Unique Constraint Violation).
 * Ném ra từ Service khi Model chuẩn bị insert/update nhưng phát hiện tên, email, sđt... đã tồn tại.
 */
public class DuplicateResourceException extends Exception {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
