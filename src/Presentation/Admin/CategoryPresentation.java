package Presentation.Admin;

import Exceptions.*;
import Models.CoreEntities.Category;
import Service.Product.CategoryService;

import java.util.List;
import java.util.Scanner;

public class CategoryPresentation {
    private CategoryService categoryService = new CategoryService();

    public void displayMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n+-------------------------------------------------+");
            System.out.println("|               QUẢN LÝ DANH MỤC                   |");
            System.out.println("+--------------------------------------------------+");
            System.out.println("|  1. Hiển thị danh sách danh mục (hãng SX)        |");
            System.out.println("|  2. Thêm danh mục                                |");
            System.out.println("|  3. Sửa danh mục                                 |");
            System.out.println("|  4. Xóa danh mục                                 |");
            System.out.println("|  0. Quay lại menu chính                          |");
            System.out.println("+--------------------------------------------------+");
            System.out.print("|  Chọn chức năng: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    displayCategories();
                    break;
                case "2":
                    addCategory(sc);
                    break;
                case "3":
                    updateCategory(sc);
                    break;
                case "4":
                    deleteCategory(sc);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ, vui lòng thử lại!");
            }
        }
    }

    private void displayCategories() {
        System.out.println("\n--- DANH SÁCH DANH MỤC ---");
        try {
            List<Category> categories = categoryService.getAllCategories();
            if (categories == null || categories.isEmpty()) {
                System.out.println("Chưa có danh mục nào.");
                return;
            }
            int stt = 1;
            System.out.printf("%-5s | %-5s | %-30s\n","STT", "ID", "TÊN DANH MỤC");
            System.out.println("------+---------------------------------");
            for (Category c : categories) {
                System.out.printf("%-5d |%-5d | %-30s\n",stt, c.getId(), c.getName());
                stt++;
            }
        } catch (DatabaseException e) {
            System.out.println("Lỗi hệ thống: " + e.getMessage());
        }
    }

    private void addCategory(Scanner sc) {
        System.out.println("\n--- THÊM DANH MỤC MỚI ---");
        System.out.print("Nhập tên danh mục: ");
        String name = sc.nextLine();
        try {
            if (categoryService.addCategory(name)) {
                System.out.println("-> Thêm thành công!");
            }
        } catch (InvalidInputException | DuplicateResourceException e) {
            System.out.println("-> Lỗi: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("-> Lỗi hệ thống: " + e.getMessage());
        }
    }

    private void updateCategory(Scanner sc) {
        System.out.println("\n--- SỬA DANH MỤC ---");
        System.out.print("Nhập ID danh mục cần sửa: ");
        int id;
        try {
            id = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("-> Lỗi: ID phải là số nguyên!");
            return;
        }
        System.out.print("Nhập tên danh mục mới: ");
        String name = sc.nextLine();

        try {
            if (categoryService.updateCategory(id, name)) {
                System.out.println("-> Sửa thành công!");
            }
        } catch (InvalidInputException | DuplicateResourceException e) {
            System.out.println("-> Lỗi: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("-> Lỗi hệ thống: " + e.getMessage());
        }
    }

    private void deleteCategory(Scanner sc) {
        System.out.println("\n--- XÓA DANH MỤC (XÓA MỀM) ---");
        System.out.print("Nhập ID danh mục cần xóa: ");
        int id;
        try {
            id = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("-> Lỗi: ID phải là số nguyên!");
            return;
        }

        System.out.print("Bạn có chắc chắn muốn xóa danh mục này (Y/N)? ");
        String confirm = sc.nextLine();
        if (confirm.equalsIgnoreCase("Y")) {
            try {
                if (categoryService.deleteCategory(id)) {
                    System.out.println("-> Xóa thành công!");
                }
            } catch (InvalidInputException e) {
                System.out.println("-> Lỗi: " + e.getMessage());
            } catch (DatabaseException e) {
                System.out.println("-> Lỗi hệ thống: " + e.getMessage());
            }
        } else {
            System.out.println("-> Hủy thao tác xóa.");
        }
    }
}
