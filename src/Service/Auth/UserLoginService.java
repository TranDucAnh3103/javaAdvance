package Service.Auth;

import DAO.Auth.UserLoginDAO;
import Exceptions.*;
import Models.CoreEntities.User;
import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Tầng Service (Business Logic Layer) chịu trách nhiệm kiểm tra tính hợp lệ của dữ liệu
 * trước khi gọi xuống DAO (Data Access Layer). Không thao tác I/O với người dùng ở đây.
 */
public class UserLoginService {

    private UserLoginDAO userDAO = new UserLoginDAO();

    // dang nhap
    public User login(String email, String rawPassword) throws InvalidInputException, DatabaseException {
        if (email == null || email.trim().isEmpty() || rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new InvalidInputException("Email và mật khẩu không được phép rỗng.");
        }
        
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
            throw new InvalidInputException("Email hoặc mật khẩu không đúng.");
        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Lỗi hệ thống khi đăng nhập: " + e.getMessage(), e);
        }
    }

    /**
     * Tầng Service: Xác thực định dạng số điện thoại.
     * Hàm này kiểm tra logic (phải 10 số, bắt đầu bằng 0). 
     * Nếu sai, ném InvalidPhoneNumberException kèm theo message rõ ràng.
     */
    public void validatePhone(String phone) throws InvalidPhoneNumberException {
        if (phone == null || phone.trim().isEmpty()) {
            throw new InvalidPhoneNumberException("Số điện thoại không được để trống.");
        }
        if (!phone.matches("^0[0-9]{9}$")) {
            throw new InvalidPhoneNumberException("Số điện thoại không hợp lệ (phải gồm 10 số chữ số và bắt đầu bằng số 0).");
        }
    }

    // dang ky
    public boolean register(User user) throws InvalidInputException, InvalidEmailException, InvalidPhoneNumberException, DuplicateResourceException, DatabaseException {
        if (user == null) {
            throw new InvalidInputException("Thông tin người dùng không hợp lệ.");
        }

        if (user.getFullName() == null || user.getFullName().trim().isEmpty() ||
            user.getEmail() == null || user.getEmail().trim().isEmpty() ||
            user.getPhone() == null || user.getPhone().trim().isEmpty() ||
            user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new InvalidInputException("Các trường bắt buộc không được để trống!");
        }

        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]+$")) {
            throw new InvalidEmailException("Email không hợp lệ (ví dụ: test@gmail.com).");
        }

        // Tái sử dụng logic kiểm tra SĐT từ hàm validatePhone
        validatePhone(user.getPhone());

        try {
            // Hash password trước khi lưu
            String hashedPassword = BCrypt.withDefaults()
                    .hashToString(12, user.getPassword().toCharArray());

            user.setPassword(hashedPassword);

            return userDAO.insertUser(user);

        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            throw new DuplicateResourceException("Email hoặc Số điện thoại này đã được hệ thống sử dụng!");
        } catch (Exception e) {
            throw new DatabaseException("Đăng ký thất bại (" + e.getMessage() + ")", e);
        }
    }
}