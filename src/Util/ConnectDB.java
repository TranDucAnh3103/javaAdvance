package Util;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Connection;

public class ConnectDB {

    // DataSource dùng để quản lý connection pool
    private static DataSource dataSource;

    // Khối static khởi tạo pool khi class được load (singleton pattern)
    static {
        PoolProperties p = new PoolProperties();

        // Thông tin kết nối database
        p.setUrl("jdbc:mysql://localhost:3306/Project_DB");
        p.setDriverClassName("com.mysql.cj.jdbc.Driver");
        p.setUsername("root");
        p.setPassword("310306");

        // CẤU HÌNH CONNECTION POOL

        // Số connection tối đa trong pool
        p.setMaxActive(20);

        // Số connection được tạo sẵn ban đầu
        p.setInitialSize(5);

        // Thời gian tối đa (ms) chờ lấy connection trước khi timeout
        p.setMaxWait(10000);

        // (Optional) kiểm tra connection còn sống trước khi sử dụng
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");

        // Khởi tạo DataSource với cấu hình trên
        dataSource = new DataSource();
        dataSource.setPoolProperties(p);
    }

    /**
     * Lấy connection từ pool
     * @return Connection đã được quản lý bởi pool
     * @throws Exception nếu không lấy được connection
     */
    public static Connection getConnection() throws Exception {
        return dataSource.getConnection();
    }
}


/*
 * ================== TOMCAT JDBC CONNECTION POOL ==================
 * Thư viện: org.apache.tomcat.jdbc.pool (tomcat-jdbc)
 *
 * Mục đích:
 * - Quản lý và tái sử dụng các kết nối (Connection) tới database.
 * - Tránh việc tạo/đóng connection liên tục (tốn tài nguyên, chậm).
 *
 * Cách hoạt động:
 * - Khi ứng dụng cần Connection → lấy từ pool (nếu có sẵn).
 * - Khi gọi conn.close() → connection KHÔNG bị hủy, mà được trả lại pool.
 * - Pool sẽ tự quản lý số lượng connection dựa trên cấu hình.
 *
 * Lợi ích:
 * - Tăng hiệu năng (giảm chi phí tạo connection mới)
 * - Giảm tải cho database
 * - Kiểm soát số lượng connection đồng thời
 *
 * Các cấu hình quan trọng:
 * - maxActive     : số connection tối đa trong pool
 * - initialSize   : số connection tạo sẵn khi khởi động
 * - maxWait       : thời gian chờ khi pool hết connection
 * - testOnBorrow  : kiểm tra connection trước khi cấp phát
 * - validationQuery: câu SQL dùng để kiểm tra connection (ví dụ: SELECT 1)
 *
 * Lưu ý:
 * - Luôn gọi conn.close() sau khi dùng (để trả về pool)
 * - Không lạm dụng maxActive quá lớn → có thể làm nghẽn database
 * - Nếu không dùng pool → DriverManager sẽ chậm hơn đáng kể
 *
 * Khi nào dùng:
 * - Ứng dụng có nhiều request / truy vấn DB thường xuyên
 * - Web app, backend service, API
 * ================================================================
 */