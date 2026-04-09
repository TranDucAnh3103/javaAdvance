package Exceptions;

/**
 * Custom Exception bọc toàn bộ các lỗi liên quan đến SQLExceptions hay hệ thống CSDL.
 * Giúp cô lập SQL logic ở DAO, không để rò rỉ mã lỗi kỹ thuật SQL trực tiếp ra Presentation.
 */
public class DatabaseException extends Exception {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DatabaseException(String message) {
        super(message);
    }
}
