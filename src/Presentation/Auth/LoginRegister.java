package Presentation.Auth;

import Exceptions.*;
import Models.CoreEntities.User;
import Presentation.Admin.AdminMenu;
import Presentation.Customer.CustomerMenu;
import Service.Auth.UserLoginService;

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

        try {
            User user = userService.login(email, pass);
            // Lưu trạng thái đăng nhập vào Session -> để getCurrentRole lấy luôn role để so sánh.
            Util.Session.setLoggedInUser(user);

            System.out.println("Chào mừng " + user.getFullName() + " [" + user.getRole() + "]");
            
            if (User.UserRole.ADMIN.equals(user.getRole())) {
                AdminMenu adminMenu = new AdminMenu();
                adminMenu.displayAdminMenu();
            } else if (User.UserRole.CUSTOMER.equals(user.getRole())) {
                CustomerMenu customerMenu = new CustomerMenu();
                customerMenu.displayCustomerMenu();
            } else {
                System.out.println("Vai trò người dùng không hợp lệ!");
            }
        } catch (InvalidInputException e) {
            System.out.println("Lỗi đăng nhập: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("Lỗi hệ thống: " + e.getMessage());
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

        String phone = "";
        while (true) {
            System.out.print  ("|  SĐT (* - 10 số)      : ");
            phone = sc.nextLine().trim();
            try {
                // Tầng Presentation gọi Service để kiểm duyệt dữ liệu nhập
                userService.validatePhone(phone);
                break; // Thoát vòng lặp nếu phone hợp lệ
            } catch (InvalidPhoneNumberException e) {
                // Bắt Exception và hiển thị thông báo lỗi màu đỏ, sau đó yêu cầu nhập lại
                System.err.println("Lỗi: " + e.getMessage());
            }
        }

        System.out.print  ("|  Địa chỉ              : ");
        String address = sc.nextLine().trim();

        System.out.print  ("|  Mật khẩu (*)         : ");
        String pass = sc.nextLine().trim();

        System.out.println("+--------------------------------------------------+");

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
            }
        } catch (InvalidPhoneNumberException | InvalidEmailException | InvalidInputException e) {
            System.out.println("Lỗi đầu vào: " + e.getMessage());
        } catch (DuplicateResourceException e) {
            System.out.println("Lỗi đăng ký: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("Lỗi hệ thống: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Lỗi nghiêm trọng: Có lỗi xảy ra trong quá trình khởi tạo tài khoản -> " + e.getMessage());
        }
    }
}
