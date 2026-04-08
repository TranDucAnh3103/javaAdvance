package Presentation;

import java.util.Scanner;

public class Customer {
    public void displayCustomerMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("+--------------------------------------------------+");
            System.out.println("|              KHÁCH HÀNG                          |");
            System.out.println("+--------------------------------------------------+");
            System.out.println("|  1. Xem danh sách sản phẩm                       |");
            System.out.println("|  2. Tìm kiếm sản phẩm                            |");
            System.out.println("|  3. Quản lý giỏ hàng & Đặt hàng                  |");
            System.out.println("|  4. Xem lịch sử đơn hàng                         |");
            System.out.println("|  0. Đăng xuất                                    |");
            System.out.println("+--------------------------------------------------+");
            System.out.print  ("|  Chọn chức năng: ");
            
            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    System.out.println("--> Chức năng Xem danh sách sản phẩm đang được hoàn thiện...");
                    break;
                case "2":
                    System.out.println("--> Chức năng Tìm kiếm sản phẩm đang được hoàn thiện...");
                    break;
                case "3":
                    System.out.println("--> Chức năng Quản lý giỏ hàng & Đặt hàng đang được hoàn thiện...");
                    break;
                case "4":
                    System.out.println("--> Chức năng Xem lịch sử đơn hàng đang được hoàn thiện...");
                    break;
                case "0":
                    Util.Session.clear();
                    System.out.println("Đã đăng xuất khỏi tài khoản Customer.");
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ, vui lòng thử lại!");
            }
        }
    }
}
