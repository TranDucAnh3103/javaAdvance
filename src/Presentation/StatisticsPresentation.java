package Presentation;

import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;
import Models.DTO.TopProductDTO;
import Service.StatisticsService;
import Service.StatisticsServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Màn hình báo cáo thống kê cho Admin.
 * Hiện tại hỗ trợ tính năng: Top 5 (hoặc N) sản phẩm bán chạy nhất tháng.
 */
public class StatisticsPresentation {

    private StatisticsService statsService = new StatisticsServiceImpl();

    public void displayMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n+--------------------------------------------------+");
            System.out.println("|             BÁO CÁO THỐNG KÊ (ADMIN)              |");
            System.out.println("+--------------------------------------------------+");
            System.out.println("|  1. Top sản phẩm bán chạy nhất tháng            |");
            System.out.println("|  0. Quay lại menu chính                          |");
            System.out.println("+--------------------------------------------------+");
            System.out.print("|  Chọn chức năng: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    topSellingProducts(sc);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    /**
     * Hiển thị Top N sản phẩm bán chạy nhất.
     * Admin chọn tháng/năm (mặc định là tháng hiện tại) và số lượng Top muốn xem.
     */
    private void topSellingProducts(Scanner sc) {
        System.out.println("\n--- TOP SẢN PHẨM BÁN CHẠY NHẤT ---");

        // Gợi ý tháng/năm hiện tại để Admin khỏi phải nhớ
        LocalDate now = LocalDate.now();
        System.out.print("Nhập tháng (1-12) [Mặc định: " + now.getMonthValue() + "]: ");
        String monthStr = sc.nextLine().trim();
        int month = monthStr.isEmpty() ? now.getMonthValue() : Integer.parseInt(monthStr);

        System.out.print("Nhập năm [Mặc định: " + now.getYear() + "]: ");
        String yearStr = sc.nextLine().trim();
        int year = yearStr.isEmpty() ? now.getYear() : Integer.parseInt(yearStr);

        System.out.print("Số lượng Top muốn xem [Mặc định: 5]: ");
        String limitStr = sc.nextLine().trim();
        int limit = limitStr.isEmpty() ? 5 : Integer.parseInt(limitStr);

        try {
            List<TopProductDTO> results = statsService.getTopSellingProducts(month, year, limit);

            if (results.isEmpty()) {
                System.out.println("=> Không có dữ liệu bán hàng trong tháng " + month + "/" + year + ".");
                System.out.println("   (Có thể chưa có đơn hàng nào, hoặc tất cả đơn đều bị CANCELLED)");
                return;
            }

            System.out.println("\n" + "=".repeat(80));
            System.out.printf("   TOP %d SẢN PHẨM BÁN CHẠY NHẤT — THÁNG %d/%d%n", limit, month, year);
            System.out.println("=".repeat(80));
            System.out.printf("| %-5s | %-35s | %-12s | %-16s |%n",
                    "Hạng", "Tên sản phẩm", "SL đã bán", "Doanh thu (VNĐ)");
            System.out.println("-".repeat(80));

            int rank = 1;
            for (TopProductDTO dto : results) {
                // Đánh dấu đặc biệt cho Top 1 — vì ai mà không thích nhìn cái huy chương vàng :)
                String rankLabel = (rank == 1) ? "🥇 1" : (rank == 2) ? "🥈 2" : (rank == 3) ? "🥉 3" : "   " + rank;
                System.out.printf("| %-5s | %-35s | %-12d | %-16.2f |%n",
                        rankLabel,
                        dto.getProductName(),
                        dto.getTotalSold(),
                        dto.getRevenue());
                rank++;
            }

            System.out.println("=".repeat(80));
            System.out.println("(*) Chỉ tính đơn hàng không bị hủy (CANCELLED).");

        } catch (NumberFormatException e) {
            System.err.println("=> Lỗi: Tháng, năm, số lượng phải là số nguyên!");
        } catch (InvalidInputException e) {
            System.err.println("=> Lỗi validate: " + e.getMessage());
        } catch (DatabaseException e) {
            System.err.println("=> Lỗi hệ thống: " + e.getMessage());
        }
    }
}
