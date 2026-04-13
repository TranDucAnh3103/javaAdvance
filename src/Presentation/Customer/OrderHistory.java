package Presentation.Customer;

import Exceptions.DatabaseException;
import Models.CoreEntities.User;
import Models.SalesOrders.Order;
import Service.Sales.OrderService;
import Util.Session;

import java.util.List;

/**
 * Module xem lịch sử đơn hàng cho Customer.
 * Hiển thị bảng đơn hàng cá nhân với trạng thái và tổng tiền.
 *
 * Tách từ Customer.java gốc: viewOrderHistory()
 */
public class OrderHistory {

    private OrderService os;

    public OrderHistory(OrderService os) {
        this.os = os;
    }

    public void viewOrderHistory() {
        System.out.println("\n--- LỊCH SỬ THANH TOÁN ---");
        try {
            User u = Session.getLoggedInUser();
            List<Order> orders = os.getOrderHistory(u.getId());
            
            if (orders.isEmpty()) {
                System.out.println("=> Bạn chưa từng mua thứ gì.");
                return;
            }

            System.out.println("=".repeat(120));
            System.out.printf("| %-10.10s | %-20.20s | %-15.15s | %-15.15s | %-40.40s |%n", 
                    "Mã ĐH", "Ngày Mua", "Trạng Thái", "Tổng Bill (VNĐ)", "Địa chỉ");
            System.out.println("-".repeat(120));
            
            for (Order o : orders) {
                System.out.printf("| %-10d | %-20.20s | %-15.15s | %-15.2f | %-40.40s |%n",
                        o.getId(),
                        o.getCreatedAt().toString(),
                        o.getStatus().toString(),
                        o.getTotalAmount(),
                        o.getShippingAddress());
            }
            System.out.println("=".repeat(120));
            System.out.println("Lưu ý nhỏ: Cần hỗ trợ Hủy đơn hàng xin liên hệ Hotline (0.981.247.641). ");

        } catch (DatabaseException e) {
            System.err.println("=> lỗi kết nối SQL : " + e.getMessage());
        }
    }
}
