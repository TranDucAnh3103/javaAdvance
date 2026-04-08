package Util;

import Models.CoreEntities.User;

public class Session {
    // Lưu lại thông tin người dùng đang thao tác
    private static User loggedInUser = null;

    // Lấy đối tượng user đang đăng nhập
    public static User getLoggedInUser() {
        return loggedInUser;
    }

    // Thiết lập user khi đăng nhập thành công
    public static void setLoggedInUser(User user) {
        loggedInUser = user;
    }

    // Lấy trực tiếp Role của user hiện tại (trả về null nếu chưa đăng nhập)
    public static User.UserRole getCurrentRole() {
        if (loggedInUser != null) {
            return loggedInUser.getRole();
        }
        return null; // Tương đương với chưa đăng nhập
    }

    // Xóa session (khi đăng xuất hoặc tắt chương trình)
    public static void clear() {
        loggedInUser = null;
    }
}
