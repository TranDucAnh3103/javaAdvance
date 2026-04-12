package Presentation;

import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;
import Models.SalesOrders.Order;
import Service.OrderService;
import Service.OrderServiceImpl;

import java.util.List;
import java.util.Scanner;

/**
 * Màn hình quản lý đơn hàng dành riêng cho Admin.
 * Admin có thể xem toàn bộ đơn hàng của tất cả khách và cập nhật trạng thái giao hàng.
 */
public class OrderPresentation {

    private OrderService orderService = new OrderServiceImpl();

    public void displayMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n+--------------------------------------------------+");
            System.out.println("|            QUẢN LÝ ĐƠN HÀNG (ADMIN)              |");
            System.out.println("+--------------------------------------------------+");
            System.out.println("|  1. Xem toàn bộ đơn hàng                         |");
            System.out.println("|  2. Cập nhật trạng thái đơn hàng                 |");
            System.out.println("|  0. Quay lại menu chính                          |");
            System.out.println("+--------------------------------------------------+");
            System.out.print("|  Chọn chức năng: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    displayAllOrders();
                    break;
                case "2":
                    updateOrderStatus(sc);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ, vui lòng thử lại!");
            }
        }
    }

    /**
     * Hiển thị bảng đơn hàng của tất cả khách hàng.
     * JOIN bảng users nên mỗi dòng in ra cả tên khách, không phải mỗi user_id khô khan.
     */
    private void displayAllOrders() {
        System.out.println("\n--- DANH SÁCH TOÀN BỘ ĐƠN HÀNG ---");
        try {
            List<Order> orders = orderService.getAllOrders();
            if (orders.isEmpty()) {
                System.out.println("=> Hệ thống chưa có đơn hàng nào.");
                return;
            }

            // Vẽ bảng Console — canh cột sao cho đọc dễ nhất
            System.out.println("=".repeat(140));
            System.out.printf("| %-8s | %-20s | %-20s | %-12s | %-16s | %-40s |%n",
                    "Mã ĐH", "Khách hàng", "Ngày đặt", "Trạng thái", "Tổng tiền (VNĐ)", "Địa chỉ giao hàng");
            System.out.println("-".repeat(140));

            for (Order o : orders) {
                // Nếu tài khoản user bị xóa mà đơn hàng vẫn tồn tại → customerName sẽ là null
                String name = o.getCustomerName() != null ? o.getCustomerName() : "(Đã xóa TK)";
                System.out.printf("| %-8d | %-20s | %-20s | %-12s | %-16.2f | %-40s |%n",
                        o.getId(),
                        name,
                        o.getCreatedAt().toString(),
                        o.getStatus().name(),
                        o.getTotalAmount(),
                        o.getShippingAddress());
            }

            System.out.println("=".repeat(140));
            System.out.println("Tổng cộng: " + orders.size() + " đơn hàng.");
            System.out.println("Ghi chú trạng thái: PENDING (Chờ) → SHIPPING (Đang giao) → DELIVERED (Đã giao) | CANCELLED (Đã hủy)");

        } catch (DatabaseException e) {
            System.err.println("Lỗi hệ thống: " + e.getMessage());
        }
    }

    /**
     * Cho Admin chọn đơn hàng cần đổi trạng thái.
     * Luồng: Nhập mã đơn → Chọn trạng thái mới → Service validate → DAO update.
     * Nếu Admin cố chuyển sai quy trình (VD: DELIVERED quay lại PENDING), Service sẽ chặn.
     */
    private void updateOrderStatus(Scanner sc) {
        // Hiển thị bảng đơn hàng trước để Admin biết đơn nào đang ở trạng thái gì
        displayAllOrders();

        System.out.println("\n--- CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG ---");
        System.out.print("Nhập Mã đơn hàng (Order ID) cần cập nhật: ");
        try {
            int orderId = Integer.parseInt(sc.nextLine());

            // Hiển thị menu trạng thái cho Admin chọn — thay vì bắt gõ chữ dài dòng
            System.out.println("Chọn trạng thái mới:");
            System.out.println("  1. SHIPPING  (Bắt đầu giao hàng)");
            System.out.println("  2. DELIVERED (Đã giao thành công)");
            System.out.println("  3. CANCELLED (Hủy đơn hàng)");
            System.out.print("=> Chọn (1-3): ");

            String statusChoice = sc.nextLine();
            String newStatus = switch (statusChoice) {
                case "1" -> "SHIPPING";
                case "2" -> "DELIVERED";
                case "3" -> "CANCELLED";
                default -> null;
            };

            if (newStatus == null) {
                System.out.println("=> Lựa chọn không hợp lệ. Đã hủy thao tác.");
                return;
            }

            // Xác nhận lần cuối trước khi đổi — phòng trường hợp Admin bấm nhầm
            System.out.print("Xác nhận chuyển đơn #" + orderId + " sang [" + newStatus + "]? (Y/N): ");
            String confirm = sc.nextLine();
            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("=> Đã hủy thao tác.");
                return;
            }

            boolean success = orderService.updateOrderStatus(orderId, newStatus);
            if (success) {
                System.out.println("=> Cập nhật thành công! Đơn #" + orderId + " chuyển sang [" + newStatus + "].");
            } else {
                System.out.println("=> Cập nhật thất bại (Đơn hàng không tồn tại hoặc lỗi không xác định).");
            }

        } catch (NumberFormatException e) {
            System.err.println("=> Mã đơn hàng phải là số nguyên!");
        } catch (InvalidInputException e) {
            System.err.println("=> Lỗi nghiệp vụ: " + e.getMessage());
        } catch (DatabaseException e) {
            System.err.println("=> Lỗi hệ thống: " + e.getMessage());
        }
    }
}
