package Presentation;

import java.util.Scanner;

public class Admin {
    public void displayAdminMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("+--------------------------------------------------+");
            System.out.println("|              QUẢN TRỊ VIÊN (ADMIN)               |");
            System.out.println("+--------------------------------------------------+");
            System.out.println("|  1. Quản lý danh mục                             |");
            System.out.println("|  2. Quản lý sản phẩm                             |");
            System.out.println("|  3. Quản lý đơn hàng                             |");
            System.out.println("|  4. Báo cáo thống kê                             |");
            System.out.println("|  5. Quản lý Flash Sale                           |");
            System.out.println("|  6. Quản lý Mã giảm giá (Coupon)                 |");
            System.out.println("|  0. Đăng xuất                                    |");
            System.out.println("+--------------------------------------------------+");
            System.out.print("|  Chọn chức năng: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    CategoryPresentation catUI = new CategoryPresentation();
                    catUI.displayMenu();
                    break;
                case "2":
                    ProductPersentation proUI = new ProductPersentation();
                    proUI.displayProductMenu();
                    break;
                case "3":
                    OrderPresentation orderUI = new OrderPresentation();
                    orderUI.displayMenu();
                    break;
                case "4":
                    StatisticsPresentation statsUI = new StatisticsPresentation();
                    statsUI.displayMenu();
                    break;
                case "5":
                    FlashSalePresentation flashUI = new FlashSalePresentation();
                    flashUI.displayMenu();
                    break;
                case "6":
                    CouponPresentation couponUI = new CouponPresentation();
                    couponUI.displayMenu();
                    break;
                case "0":
                    Util.Session.clear();
                    System.out.println("Đã đăng xuất khỏi tài khoản Admin.");
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ, vui lòng thử lại!");
            }
        }
    }
}
