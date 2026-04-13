package DAO.Auth;

import Models.CoreEntities.User;
import Util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserLoginDAO {
    // Task: Kiểm tra đăng nhập với DB
    public User getUserByEmail(String email) throws Exception {
        String sql = "SELECT * FROM users WHERE email = ? AND is_active = TRUE";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Map data từ DB sang Object User [cite: 63]
                    return new User(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("password"),
                            rs.getString("address"),
                            User.UserRole.valueOf(rs.getString("role").toUpperCase().trim()),
                            rs.getTimestamp("created_at"),
                            rs.getBoolean("is_active")
                    );
                }
            }
        }
        return null; // Không tìm thấy User
    }

    // Task: Đăng ký tài khoản
    public boolean insertUser(User user) throws Exception {
        String sql = "INSERT INTO users (full_name, email, phone, password, address, role) VALUES (?, ?, ?, ?, ?, 'CUSTOMER')";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getAddress());

            return ps.executeUpdate() > 0;
        }
        // Nếu trùng email/phone
        //-> không bắt lỗi, khi có lỗi trong executeUpdate, nó ném throw về SQLexception
        // tầng service bắt lỗi này và kiểm tra true/false để tách biệt việc kết nối và logic
    }
}