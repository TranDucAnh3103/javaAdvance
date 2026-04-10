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
            String filterStr = currentSearchQuery == null ? "Không" : ("'" + currentSearchQuery + "'");
            String sortStr = currentSortBy == null ? "Mặc định (Tăng dần ID)" : (currentSortBy + " " + currentSortOrder);
            System.out.printf("| Lọc: %-20s | Sắp xếp: %-22s |%n", filterStr, sortStr);
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
        int limit = 5; // Số lượng sản phẩm tối đa hiển thị trên 1 trang 
        int page = 1; // Khởi tạo biến trang hiện tại, mặc định bắt đầu ở trang số 1

        while (true) { // Vòng lặp vô hạn giúp người dùng giữ nguyên trạng thái danh sách để liên tục chuyển trang
            try {
                // Đếm tổng số lượng sản phẩm dựa trên DB. Việc truyền tham số 'currentSearchQuery' giúp nó đếm dựa vào bộ lọc tìm kiếm hiện tại chứ không phải đếm TẤT CẢ.
                int totalProducts = ps.getTotalProducts(currentSearchQuery, false);
                
                // Nếu tổng số trả về <= 0 (trong DB không có sản phẩm nào, hoặc tìm kiếm không khớp), in thông báo chặn lỗi và trở về menu trước.
                if (totalProducts <= 0) {
                    System.out.println("=> Không tìm thấy sản phẩm nào.");
                    return;
                }

                // Tính tổng số trang bằng cách: Tổng_Sản_Phẩm chia cho Số_SP_1_Trang (limit). Toán tử Math.ceil dùng để làm tròn LÊN. (VD: 11 / 5 = 2.2 -> Lên 3 Trang)
                int totalPages = (int) Math.ceil((double) totalProducts / limit);
                
                // Validate phân trang 1: Nếu người dùng (code) làm trang hiện tại bị nhỏ hơn 1 -> Ép trang hiện tại quay về đầu (1).
                if (page < 1) page = 1;
                // Validate phân trang 2: Nếu người dùng cố qua trang lớn hơn trang chỉ định -> Ép dừng ở lại ngay chốt chặn tổng trang (totalPages)
                if (page > totalPages) page = totalPages;

                // Tính toán tham số offset. Offset biểu thị cho 'số lượng bản ghi mà truy vấn sẽ lướt qua' trước khi bắt đầu lấy dòng dữ liệu. 
                // Công thức: (Trang hiện tại - 1) * Số bản ghi 1 trang. Ví dụ để hiển thị Trang 2 -> (2-1)*5 = 5. Lướt qua 5 bản ghi và lấy bản ghi 6.
                int offset = (page - 1) * limit;

                // Call DAO/Service lấy List dữ liệu thực sự với đủ 5 tham số: phân trang, bỏ qua bao nhiêu, tìm kiếm chữ gì, sắp xếp field nào, chiều nào
                List<ProductDTO> list = ps.getAllProducts(limit, offset, currentSearchQuery, currentSortBy, currentSortOrder, false);

                // ============= BẮT ĐẦU BLOCK RENDER GIAO DIỆN CONSOLE =============
                // Vẽ một đường kẻ ngang trang trí khớp với độ dài bảng (124 ký tự)
                System.out.println("\n" + "=".repeat(124)); 
                
                // In Header của cột. Kí hiệu %-X s: chuỗi hiển thị tốn X kí tự space, dấu '-' căn lề Trái.
                System.out.printf("| %-15s | %-30s | %-20s | %-15s | %-15s | %-10s |%n",
                        "Danh mục", "Tên sản phẩm", "Cấu hình", "Màu", "Giá", "Kho");
                System.out.println("-".repeat(124));

                // Bắt đầu vòng lặp lấy từng DTO Product bên trong cái LIST mới tải DB về
                for (ProductDTO p : list) {
                    // Nối RAM và ROM
                    String specs = p.getRam() + "/" + p.getRom();
                    
                    // Khoảng cách được bung rộng ra để phòng trường hợp chuỗi tên Sản phẩm hoặc Danh mục dài.
                    System.out.printf("| %-15s | %-30s | %-20s | %-15s | %-15.2f | %-10d |%n",
                            p.getCategoryName(),
                            p.getProductName(),
                            specs,
                            p.getColor(),
                            p.getPrice(),
                            p.getStock());
                }
                
                System.out.println("=".repeat(124));
                
                // In ra thông tin Footer, thông báo vị trí đứng (VD: Trang 1 / 3 (Tổng: 10 sản phẩm))
                System.out.println("Trang " + page + " / " + totalPages + " (Tổng: " + totalProducts + " sản phẩm)");
                // In hướng dẫn bấm lệnh
                System.out.println("Điều hướng: [n] Trang tiếp - [p] Trang trước - [số trang] Đi đến trang cụ thể - [0] Thoát danh sách");
                System.out.print("=> Lựa chọn: ");

                // ============= BLOCK XỬ LÝ ĐIỀU HƯỚNG TỪ INPUT =============
                // Nhận đầu vào (Trim xóa khoẳng trắng bị nhập lầm dư thừa) và (ToLowerCase chuyển hết qua kí tự viết Thường (để P/N/n/p đều bắt được))
                String cmd = sc.nextLine().trim().toLowerCase();
                
                if (cmd.equals("0")) { // Thoát khỏi Vòng Lặp Vô Hạn bên trên để trở về Menu Admin
                    break;
                } else if (cmd.equals("n")) { // Lệnh n (Next): Chuyển trang tiếp
                    // Chỉ cho tăng +1 nếu điều kiện Trang Mới còn nhỏ hơn Tổng Trang thiết lập
                    if (page < totalPages) page++; 
                    else System.out.println("=> Đã ở trang cuối cùng!"); 
                } else if (cmd.equals("p")) { // Lệnh p (Previous): Lùi 1 trang
                    // Chỉ cho lùi -1 khi Trang lùi về vẫn không bị văng số Âm. (Giới hạn của lùi là bằng 1)
                    if (page > 1) page--; 
                    else System.out.println("=> Đã ở trang đầu tiên!");
                } else { 
                    // Trường hợp người dùng không gõ p hay n hay 0, mà họ gõ đại 1 số Trang. Vd mún qua Trang "3" luôn. (Hoặc cố tình gõ kí tự nhảm)
                    try {
                        // Thử ép kiểu giá trị người dùng nhập vào -> Biến kiểu Số Nguyên
                        int pStr = Integer.parseInt(cmd); 
                        // Nếu Ép Kiểu thành công và Lớn hơn mức Tối Thiểu (1), Nhỏ hơn mức Tối Đa (Max_Page)
                        if (pStr >= 1 && pStr <= totalPages) { 
                            page = pStr; // Gán lại Trang Cứng thành công -> Next Vòng Lặp -> Render lại Data Trang đó
                        } else {
                            System.out.println("=> Trang nhập vào không hợp lệ!"); // Báo lỗi Out of Bounds Array (Ngoài tầm đếm)
                        }
                    } catch (NumberFormatException e) {
                        // Bắt lỗi Nếu người ta gõ chữ Text chèn vào thì parseInt sẽ đâm Lỗi Exception (Fail)
                        System.out.println("=> Lệnh điều hướng không hợp lệ!"); 
                    }
                }
            } catch (DatabaseException e) {
                // Bắt lỗi nếu DatabaseException từ DAO/Service văng lên (ví dụ Rớt mạng DB)
                System.out.println("Lỗi hệ thống: " + e.getMessage());
                break; // Hủy Table, văng màn
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
