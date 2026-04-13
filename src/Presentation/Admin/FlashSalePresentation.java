package Presentation.Admin;

import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;
import Models.MarketingPromotions.FlashSale;
import Models.MarketingPromotions.FlashSaleItem;
import Service.Promotion.FlashSaleService;
import Service.Promotion.FlashSaleServiceImpl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Giao diện quản lý Flash Sale cho Admin.
 * Admin tạo sự kiện → thêm SP vào sự kiện → xem danh sách sự kiện.
 * Phía Customer sẽ thấy giá Flash trực tiếp trên màn hình mua sắm (xử lý ở CustomerMenu).
 */
public class FlashSalePresentation {

    private FlashSaleService flashSaleService = new FlashSaleServiceImpl();
    // Format thời gian: "2025-12-31 23:59" — dễ nhập, dễ đọc
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void displayMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n+--------------------------------------------------+");
            System.out.println("|             QUẢN LÝ FLASH SALE (ADMIN)            |");
            System.out.println("+--------------------------------------------------+");
            System.out.println("|  1. Tạo sự kiện Flash Sale mới                   |");
            System.out.println("|  2. Thêm sản phẩm vào Flash Sale                 |");
            System.out.println("|  3. Xem tất cả sự kiện Flash Sale                |");
            System.out.println("|  4. Xem SP trong 1 sự kiện Flash Sale            |");
            System.out.println("|  0. Quay lại menu chính                          |");
            System.out.println("+--------------------------------------------------+");
            System.out.print("|  Chọn chức năng: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    createFlashSale(sc);
                    break;
                case "2":
                    addItemToFlashSale(sc);
                    break;
                case "3":
                    viewAllFlashSales();
                    break;
                case "4":
                    viewFlashSaleItems(sc);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    /**
     * Tạo sự kiện Flash Sale: Admin nhập tên + khoảng thời gian.
     * Sự kiện này chỉ là "khung" — sau khi tạo xong phải thêm SP vào (menu 2) mới có hàng bán.
     */
    private void createFlashSale(Scanner sc) {
        System.out.println("\n--- TẠO SỰ KIỆN FLASH SALE MỚI ---");

        System.out.print("Tên sự kiện (VD: Flash Friday, Sale Tết...): ");
        String name = sc.nextLine();

        System.out.println("Nhập thời gian theo định dạng: yyyy-MM-dd HH:mm");
        System.out.print("Thời gian bắt đầu: ");
        String startStr = sc.nextLine();

        System.out.print("Thời gian kết thúc: ");
        String endStr = sc.nextLine();

        try {
            // Parse chuỗi thời gian → LocalDateTime → Timestamp (để JDBC hiểu được)
            Timestamp startTime = Timestamp.valueOf(LocalDateTime.parse(startStr, DTF));
            Timestamp endTime = Timestamp.valueOf(LocalDateTime.parse(endStr, DTF));

            FlashSale fs = new FlashSale();
            fs.setName(name);
            fs.setStartTime(startTime);
            fs.setEndTime(endTime);
            fs.setActive(true); // Mặc định active khi tạo mới

            int newId = flashSaleService.createFlashSale(fs);
            System.out.println("=> Tạo thành công! Flash Sale ID = " + newId);
            System.out.println("=> Tiếp theo hãy vào menu số 2 để thêm sản phẩm vào sự kiện này.");

        } catch (DateTimeParseException e) {
            System.err.println("=> Lỗi: Định dạng thời gian sai! Phải đúng kiểu: yyyy-MM-dd HH:mm (VD: 2025-06-15 09:00)");
        } catch (InvalidInputException e) {
            System.err.println("=> Lỗi validation: " + e.getMessage());
        } catch (DatabaseException e) {
            System.err.println("=> Lỗi hệ thống: " + e.getMessage());
        }
    }

    /**
     * Gắn 1 variant vào Flash Sale.
     * Admin cần biết: Flash Sale ID (từ menu 3) và Variant ID (từ trang quản lý SP).
     */
    private void addItemToFlashSale(Scanner sc) {
        System.out.println("\n--- THÊM SẢN PHẨM VÀO FLASH SALE ---");

        try {
            System.out.print("Nhập Flash Sale ID: ");
            int fsId = Integer.parseInt(sc.nextLine());

            System.out.print("Nhập Variant ID (Mã SP muốn giảm giá): ");
            int variantId = Integer.parseInt(sc.nextLine());

            System.out.print("Phần trăm giảm giá (VD: 15 nghĩa là giảm 15%): ");
            BigDecimal discountPercent = new BigDecimal(sc.nextLine());

            System.out.print("Số lượng dành riêng cho Flash Sale: ");
            int saleStock = Integer.parseInt(sc.nextLine());

            FlashSaleItem item = new FlashSaleItem();
            item.setFlashSaleId(fsId);
            item.setVariantId(variantId);
            item.setDiscountPercent(discountPercent);
            item.setSaleStock(saleStock);

            if (flashSaleService.addFlashSaleItem(item)) {
                System.out.println("=> Thêm thành công! Variant #" + variantId + " giờ giảm " + discountPercent + "% trong Flash Sale #" + fsId);
            }

        } catch (NumberFormatException e) {
            System.err.println("=> Lỗi: Dữ liệu số không hợp lệ!");
        } catch (InvalidInputException e) {
            System.err.println("=> Lỗi validate: " + e.getMessage());
        } catch (DatabaseException e) {
            System.err.println("=> Lỗi hệ thống: " + e.getMessage());
        }
    }

    /**
     * Xem tất cả sự kiện Flash Sale — kể cả đã kết thúc.
     * Đánh dấu sự kiện nào đang ACTIVE để Admin biết cái nào đang chạy.
     */
    private void viewAllFlashSales() {
        System.out.println("\n--- DANH SÁCH TẤT CẢ SỰ KIỆN FLASH SALE ---");
        try {
            List<FlashSale> sales = flashSaleService.getAllFlashSales();
            if (sales.isEmpty()) {
                System.out.println("=> Chưa có sự kiện Flash Sale nào.");
                return;
            }

            System.out.println("=".repeat(110));
            System.out.printf("| %-5s | %-25s | %-20s | %-20s | %-12s |%n",
                    "ID", "Tên sự kiện", "Bắt đầu", "Kết thúc", "Trạng thái");
            System.out.println("-".repeat(110));

            // Xác định sự kiện nào đang active bằng cách so sánh thời gian hiện tại
            Timestamp now = new Timestamp(System.currentTimeMillis());
            for (FlashSale fs : sales) {
                String statusLabel;
                if (!fs.isActive()) {
                    statusLabel = "TẮT";
                } else if (now.before(fs.getStartTime())) {
                    statusLabel = "SẮP DIỄN RA";
                } else if (now.after(fs.getEndTime())) {
                    statusLabel = "ĐÃ KẾT THÚC";
                } else {
                    statusLabel = "ĐANG CHẠY ★"; // Đánh dấu nổi bật cho sự kiện đang live
                }

                System.out.printf("| %-5d | %-25s | %-20s | %-20s | %-12s |%n",
                        fs.getId(),
                        fs.getName(),
                        fs.getStartTime().toString(),
                        fs.getEndTime().toString(),
                        statusLabel);
            }
            System.out.println("=".repeat(110));

        } catch (DatabaseException e) {
            System.err.println("=> Lỗi hệ thống: " + e.getMessage());
        }
    }

    /**
     * Xem chi tiết SP tham gia 1 Flash Sale cụ thể.
     */
    private void viewFlashSaleItems(Scanner sc) {
        System.out.print("Nhập Flash Sale ID để xem chi tiết: ");
        try {
            int fsId = Integer.parseInt(sc.nextLine());
            List<FlashSaleItem> items = flashSaleService.getFlashSaleItems(fsId);

            if (items.isEmpty()) {
                System.out.println("=> Sự kiện này chưa có sản phẩm nào.");
                return;
            }

            System.out.println("=".repeat(60));
            System.out.printf("| %-12s | %-15s | %-15s |%n", "Variant ID", "Giảm giá (%)", "SL Flash");
            System.out.println("-".repeat(60));

            for (FlashSaleItem item : items) {
                System.out.printf("| %-12d | %-15s | %-15d |%n",
                        item.getVariantId(),
                        item.getDiscountPercent().toString() + "%",
                        item.getSaleStock());
            }
            System.out.println("=".repeat(60));

        } catch (NumberFormatException e) {
            System.err.println("=> ID phải là số nguyên!");
        } catch (DatabaseException e) {
            System.err.println("=> Lỗi hệ thống: " + e.getMessage());
        }
    }
}
