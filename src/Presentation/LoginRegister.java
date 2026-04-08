package Presentation;

import Models.CoreEntities.User;
import Service.UserLoginService;

import java.util.Scanner;

public class LoginRegister {
    private UserLoginService userService = new UserLoginService();

    public void displayMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("+--------------------------------------------------+");
            System.out.println("|      HỆ THỐNG QUẢN LÝ SHOP ĐIỆN THOẠI ONLINE     |");
            System.out.println("+--------------------------------------------------+");
            System.out.println("|  1. Đăng nhập                                    |");
            System.out.println("|  2. Đăng ký                                      |");
            System.out.println("|  0. Thoát                                        |");
            System.out.println("+--------------------------------------------------+");
            System.out.print  ("|  Chọn chức năng: ");
            
            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    handleLogin(sc);
                    break;
                case "2":
                    handleRegister(sc);
                    break;
                case "0":
                    System.out.println("Cảm ơn bạn đã sử dụng!");
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ. Vui lòng chọn lại!");
            }
        }
    }

    private void handleLogin(Scanner sc) {
        System.out.println("+--------------------------------------------------+");
        System.out.println("|                 ĐĂNG NHẬP HỆ THỐNG               |");
        System.out.println("+--------------------------------------------------+");

        System.out.print  ("|  Email     : ");
        String email = sc.nextLine();

        System.out.print  ("|  Mật khẩu  : ");
        String pass = sc.nextLine();

        System.out.println("+--------------------------------------------------+");

        User user = userService.login(email, pass);
        if (user != null) {
            // Lưu trạng thái đăng nhập vào Session
            Util.Session.setLoggedInUser(user);

            System.out.println("Chào mừng " + user.getFullName() + " [" + user.getRole() + "]");
            
            if (User.UserRole.ADMIN.equals(user.getRole())) {
                Admin adminMenu = new Admin();
                adminMenu.displayAdminMenu();
            } else if (User.UserRole.CUSTOMER.equals(user.getRole())) {
                Customer customerMenu = new Customer();
                customerMenu.displayCustomerMenu();
            } else {
                System.out.println("Vai trò người dùng không hợp lệ!");
            }
        } else {
            System.out.println("Email hoặc mật khẩu không đúng!");
        }
    }

    private void handleRegister(Scanner sc) {
        System.out.println("+--------------------------------------------------+");
        System.out.println("|              ĐĂNG KÝ TÀI KHOẢN                  |");
        System.out.println("+--------------------------------------------------+");

        System.out.print  ("|  Họ và tên (*)        : ");
        String fullName = sc.nextLine().trim();

        System.out.print  ("|  Email (*)            : ");
        String email = sc.nextLine().trim();

        System.out.print  ("|  SĐT (* - 10 số)      : ");
        String phone = sc.nextLine().trim();

        System.out.print  ("|  Địa chỉ              : ");
        String address = sc.nextLine().trim();

        System.out.print  ("|  Mật khẩu (*)         : ");
        String pass = sc.nextLine().trim();

        System.out.println("+--------------------------------------------------+");

        // Validate đầu vào một lần, nếu sai sẽ báo lỗi và về menu chính luôn
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            System.out.println("Lỗi: Các trường có dấu (*) không được để trống!");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]+$")) {
            System.out.println("Lỗi: Email không hợp lệ (ví dụ: test@gmail.com).");
            return;
        }

        if (!phone.matches("^0[0-9]{9}$")) {
            System.out.println("Lỗi: Số điện thoại không hợp lệ (phải gồm 10 số và bắt đầu bằng số 0).");
            return;
        }

        try {
            User newUser = new User();
            newUser.setFullName(fullName);
            newUser.setEmail(email);
            newUser.setPhone(phone);
            newUser.setAddress(address);
            newUser.setPassword(pass);
            // Mặc định đăng ký mới là Customer
            newUser.setRole(User.UserRole.CUSTOMER);
            newUser.setActive(true);

            boolean success = userService.register(newUser);
            if (success) {
                System.out.println("Đăng ký thành công! Bạn có thể đăng nhập bằng tài khoản mới.");
            } else {
                System.out.println("Đăng ký thất bại. Email hoặc Số điện thoại có thể đã tồn tại, hoặc lỗi máy chủ.");
            }
        } catch (Exception e) {
            System.out.println("Lỗi nghiêm trọng: Có lỗi xảy ra trong quá trình khởi tạo tài khoản -> " + e.getMessage());
        }
    }
}
