package Presentation;

import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;
import Exceptions.ProductNotFoundException;
import Models.CoreEntities.Product;
import Models.CoreEntities.ProductVariant;
import Models.DTO.ProductDTO;
import Service.ProductService;
import Service.ProductServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class ProductPersentation {
    // Loose Coupling: Gọi Interface nhưng khởi tạo Implement
    private ProductService ps = new ProductServiceImpl(); 
    
    // Lưu trạng thái tìm kiếm/sắp xếp tĩnh cho session hiện tại
    private String currentSearchQuery = null;
    private String currentSortBy = null;
    private String currentSortOrder = "ASC";

    public void displayProductMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n+-------------------------------------------------------------+");
            System.out.println("|                  QUẢN LÝ SẢN PHẨM (ADMIN)                   |");
            System.out.println("+-------------------------------------------------------------+");
            System.out.println("| Lọc hiện tại: " + (currentSearchQuery == null ? "Không" : "'" + currentSearchQuery + "'") + 
                    " | Sắp xếp: " + (currentSortBy == null ? "Mặc định (Tăng dần ID)" : currentSortBy + " " + currentSortOrder) + " |");
            System.out.println("+-------------------------------------------------------------+");
            System.out.println("| 1. Hiển thị danh sách đầy đủ (Phân trang)                   |");
            System.out.println("| 2. Thêm mới sản phẩm (Validate giá & số lượng)              |");
            System.out.println("| 3. Sửa thông tin sản phẩm (Hiển thị thông tin cũ)           |");
            System.out.println("| 4. Xóa sản phẩm (Xác nhận Y/N)                              |");
            System.out.println("| 5. Cài đặt Cụm Tìm kiếm / Lọc                               |");
            System.out.println("| 6. Cài đặt Cụm Sắp xếp theo giá (Tăng/Giảm)                 |");
            System.out.println("| 0. Quay lại menu chính                                      |");
            System.out.println("+-------------------------------------------------------------+");
            System.out.print("| Chọn chức năng: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    displayAllProducts(sc);
                    break;
                case "2":
                    addProduct(sc);
                    break;
                case "3":
                    updateProduct(sc);
                    break;
                case "4":
                    deleteProduct(sc);
                    break;
                case "5":
                    searchProduct(sc);
                    break;
                case "6":
                    sortProduct(sc);
                    break;
                case "0":
                    return;
                default:
                    System.err.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    public void displayAllProducts(Scanner sc) {
        int limit = 5; 
        int page = 1;

        while (true) {
            try {
                // Truyền query tìm kiếm vào getTotalProducts để đếm đúng số trang
                int totalProducts = ps.getTotalProducts(currentSearchQuery);
                if (totalProducts <= 0) {
                    System.out.println("=> Không tìm thấy sản phẩm nào.");
                    return;
                }

                int totalPages = (int) Math.ceil((double) totalProducts / limit);
                if (page < 1) page = 1;
                if (page > totalPages) page = totalPages;

                int offset = (page - 1) * limit;

                // Lấy data với tham số search & sort
                List<ProductDTO> list = ps.getAllProducts(limit, offset, currentSearchQuery, currentSortBy, currentSortOrder);

                System.out.println("\n" + "=".repeat(110));
                System.out.printf("| %-12s | %-20s | %-15s | %-10s | %-12s | %-8s |%n",
                        "Danh mục", "Tên sản phẩm", "Cấu hình", "Màu", "Giá", "Kho");
                System.out.println("-".repeat(110));

                for (ProductDTO p : list) {
                    String specs = p.getRam() + "/" + p.getRom();
                    System.out.printf("| %-12s | %-20s | %-15s | %-10s | %-12.2f | %-8d |%n",
                            p.getCategoryName(),
                            p.getProductName(),
                            specs,
                            p.getColor(),
                            p.getPrice(),
                            p.getStock());
                }
                System.out.println("=".repeat(110));
                System.out.println("Trang " + page + " / " + totalPages + " (Tổng: " + totalProducts + " sản phẩm)");
                System.out.println("Điều hướng: [n] Trang tiếp - [p] Trang trước - [số trang] Đi đến trang cụ thể - [0] Thoát danh sách");
                System.out.print("=> Lựa chọn: ");

                String cmd = sc.nextLine().trim().toLowerCase();
                if (cmd.equals("0")) {
                    break;
                } else if (cmd.equals("n")) {
                    if (page < totalPages) page++;
                    else System.out.println("=> Đã ở trang cuối cùng!");
                } else if (cmd.equals("p")) {
                    if (page > 1) page--;
                    else System.out.println("=> Đã ở trang đầu tiên!");
                } else {
                    try {
                        int pStr = Integer.parseInt(cmd);
                        if (pStr >= 1 && pStr <= totalPages) {
                            page = pStr;
                        } else {
                            System.out.println("=> Trang nhập vào không hợp lệ!");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("=> Lệnh điều hướng không hợp lệ!");
                    }
                }
            } catch (DatabaseException e) {
                System.out.println("Lỗi hệ thống: " + e.getMessage());
                break;
            }
        }
    }

    private void addProduct(Scanner sc) {
        System.out.println("\n--- THÊM SẢN PHẨM MỚI ---");
        try {
            System.out.print("Nhập Category ID: ");
            int catId = Integer.parseInt(sc.nextLine());
            
            System.out.print("Tên sản phẩm: ");
            String name = sc.nextLine();
            
            System.out.print("Thương hiệu (Brand): ");
            String brand = sc.nextLine();

            System.out.print("Mô tả: ");
            String description = sc.nextLine();

            System.out.print("RAM (vd: 8GB): ");
            String ram = sc.nextLine();

            System.out.print("ROM (vd: 128GB): ");
            String rom = sc.nextLine();

            System.out.print("Màu sắc: ");
            String color = sc.nextLine();

            System.out.print("Giá bán: ");
            BigDecimal price = new BigDecimal(sc.nextLine());

            System.out.print("Số lượng tồn kho: ");
            int stock = Integer.parseInt(sc.nextLine());

            Product p = new Product();
            p.setCategoryId(catId);
            p.setName(name);
            p.setBrand(brand);
            p.setDescription(description);

            ProductVariant v = new ProductVariant();
            v.setRam(ram);
            v.setRom(rom);
            v.setColor(color);
            v.setPrice(price);
            v.setStock(stock);

            if (ps.addProduct(p, v)) {
                System.out.println("=> Thêm sản phẩm thành công!");
            } else {
                System.out.println("=> Thêm thất bại (Lỗi không xác định).");
            }
        } catch (NumberFormatException e) {
            System.err.println("=> Lỗi: Dữ liệu số (ID, Giá, Số lượng) không hợp lệ!");
        } catch (InvalidInputException e) {
            System.err.println("=> Lỗi validate: " + e.getMessage());
        } catch (DatabaseException e) {
            System.err.println("=> Lỗi Cổng DB: " + e.getMessage());
        }
    }

    private void updateProduct(Scanner sc) {
        System.out.println("\n--- CẬP NHẬT SẢN PHẨM ---");
        System.out.print("Nhập Product ID cần cập nhật: ");
        try {
            int pId = Integer.parseInt(sc.nextLine());
            // Mentor Note: Gọi Service để kiểm tra tồn tại và fetch Data
            Product oldProduct = ps.getProductById(pId);
            
            System.out.println("THÔNG TIN CŨ:");
            System.out.println("Tên: " + oldProduct.getName() + " | Brand: " + oldProduct.getBrand() + " | Category ID: " + oldProduct.getCategoryId());
            
            System.out.println("\nBẮT ĐẦU NHẬP MỚI (Enter để giữ nguyên dữ liệu cũ đối với kiểu chuỗi):");
            
            System.out.print("Tên sản phẩm mới: ");
            String name = sc.nextLine();
            if (name.trim().isEmpty()) name = oldProduct.getName();

            System.out.print("Category ID mới (Nhập -1 để bỏ qua): ");
            int catId = Integer.parseInt(sc.nextLine());
            if (catId == -1) catId = oldProduct.getCategoryId();

            System.out.print("Brand mới: ");
            String brand = sc.nextLine();
            if (brand.trim().isEmpty()) brand = oldProduct.getBrand();

            // Xin lưu ý: Để cho ứng dụng console đơn giản, ta minh họa bằng cách chỉ tạo Variant mới 
            // ghi đè lên Variant đầu tiên của product này. (Đòi hỏi biết variant ID. Ta nhập thủ công id variant nha)
            System.out.print("Nhập Variant ID của nó để update (Bắt buộc): ");
            int vId = Integer.parseInt(sc.nextLine());

            System.out.print("RAM mới: "); String ram = sc.nextLine();
            System.out.print("ROM mới: "); String rom = sc.nextLine();
            System.out.print("Giá bán mới: "); BigDecimal price = new BigDecimal(sc.nextLine());
            System.out.print("Tồn kho mới: "); int stock = Integer.parseInt(sc.nextLine());
            System.out.print("Color mới: "); String color = sc.nextLine();

            Product newP = new Product();
            newP.setId(pId);
            newP.setName(name);
            newP.setCategoryId(catId);
            newP.setBrand(brand);
            newP.setDescription(oldProduct.getDescription());

            ProductVariant newV = new ProductVariant();
            newV.setId(vId);
            newV.setProductId(pId);
            newV.setRam(ram);
            newV.setRom(rom);
            newV.setPrice(price);
            newV.setStock(stock);
            newV.setColor(color);

            ps.updateProduct(newP, newV);
            System.out.println("Cập nhật thành công!");

        } catch (NumberFormatException ex) {
             System.err.println("Dữ liệu nhập số không hợp lệ.");
        } catch (ProductNotFoundException e) {
            System.err.println("Lỗi: " + e.getMessage());
        } catch (InvalidInputException e) {
            System.err.println("Lỗi đầu vào: " + e.getMessage());
        } catch (DatabaseException e) {
            System.err.println("Lỗi DB: " + e.getMessage());
        }
    }

    private void deleteProduct(Scanner sc) {
        System.out.println("\n--- XÓA SẢN PHẨM MỀM (Soft Delete) ---");
        System.out.print("Nhập Product ID cần xóa: ");
        try {
            int pId = Integer.parseInt(sc.nextLine());
            
            // Hỏi xác nhận Y/N
            System.out.print("Bạn có chắc chắn muốn xóa sản phẩm này (y/n)? ");
            String confirm = sc.nextLine();
            if (confirm.equalsIgnoreCase("y")) {
                ps.deleteProduct(pId);
                System.out.println("Đã xóa mềm thành công (is_deleted = true).");
            } else {
                System.out.println("Đã hủy thao tác xóa.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Lỗi: ID phải là số!");
        } catch (ProductNotFoundException e) {
            System.err.println("Lỗi: " + e.getMessage());
        } catch (DatabaseException e) {
            System.err.println("Lỗi DB: " + e.getMessage());
        }
    }

    private void searchProduct(Scanner sc) {
        System.out.print("Nhập tên sản phẩm để tìm kiếm (Enter trống để hủy tìm kiếm): ");
        String query = sc.nextLine();
        if (query.trim().isEmpty()) {
            this.currentSearchQuery = null;
            System.out.println("Đã xóa bộ lọc tìm kiếm.");
        } else {
            this.currentSearchQuery = query;
            System.out.println("Đã lưu bộ lọc tìm kiếm. Quay lại Menu 1 để xem.");
        }
    }

    private void sortProduct(Scanner sc) {
        System.out.println("1. Sắp xếp giá TĂNG DẦN (ASC)");
        System.out.println("2. Sắp xếp giá GIẢM DẦN (DESC)");
        System.out.println("0. Hủy sắp xếp");
        System.out.print("=> Chọn: ");
        String choice = sc.nextLine();
        if ("1".equals(choice)) {
            this.currentSortBy = "price";
            this.currentSortOrder = "ASC";
            System.out.println("Đã cài đặt Sort: Price ASC");
        } else if ("2".equals(choice)) {
            this.currentSortBy = "price";
            this.currentSortOrder = "DESC";
            System.out.println("Đã cài đặt Sort: Price DESC");
        } else {
            this.currentSortBy = null;
            System.out.println("Đã xóa thuộc tính Sắp xếp.");
        }
    }
}
