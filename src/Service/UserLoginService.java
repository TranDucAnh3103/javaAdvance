package Service;

import DAO.UserLoginDAO;
import Models.CoreEntities.User;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class UserLoginService {

    private UserLoginDAO userDAO = new UserLoginDAO();

    public User login(String email, String rawPassword) {
        try {
            User user = userDAO.getUserByEmail(email);

            if (user != null) {
                String hashedPassword = user.getPassword();

                BCrypt.Result result = BCrypt.verifyer()
                        .verify(rawPassword.toCharArray(), hashedPassword);

                if (result.verified) {
                    return user; // OK, password đúng
                }
            }

        } catch (Exception e) {
            System.err.println("Lỗi hệ thống khi đăng nhập: " + e.getMessage());
        }
        return null;
    }

    public boolean register(User user) {
        if (user == null || user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            System.err.println("Lỗi: Thông tin người dùng hoặc mật khẩu không hợp lệ.");
            return false;
        }

        try {
            // Hash password trước khi lưu
            String hashedPassword = BCrypt.withDefaults()
                    .hashToString(12, user.getPassword().toCharArray());

            user.setPassword(hashedPassword);

            return userDAO.insertUser(user);

        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            System.err.println("Lỗi: Email hoặc Số điện thoại này đã được hệ thống sử dụng!");
            return false;
        } catch (Exception e) {
            System.err.println("Đăng ký thất bại: Hệ thống gặp gián đoạn (" + e.getMessage() + ")");
            return false;
        }
    }
}