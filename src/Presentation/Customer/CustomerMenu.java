package Presentation.Customer;

import Models.DTO.CartItem;
import Service.Product.ProductService;
import Service.Product.ProductServiceImpl;
import Service.Sales.OrderService;
import Service.Sales.OrderServiceImpl;
import Service.Promotion.FlashSaleService;
import Service.Promotion.FlashSaleServiceImpl;
import Service.Promotion.CouponService;
import Service.Promotion.CouponServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Hub điều phối chính cho Customer — giữ state (giỏ hàng, từ khóa tìm kiếm)
 * và phân luồng tới các module con: ProductBrowse, CartManager, CheckoutHandler, OrderHistory.
 *
 * Lý do tách: File Customer.java gốc 504 dòng quá lớn, khó debug.
 * Giờ mỗi module chỉ phụ trách 1 nghiệp vụ cụ thể, dễ tìm lỗi.
 */
public class CustomerMenu {

    // ====== SHARED STATE (Trạng thái dùng chung giữa các module) ======
    // Giỏ hàng lưu trữ tạm trên RAM trong phiên làm việc
    private List<CartItem> cart = new ArrayList<>();
    // Lưu phiên tìm kiếm
    private String currentSearchQuery = null;

    // ====== SERVICE DEPENDENCIES (Khai báo 1 lần, chia sẻ qua constructor) ======
    private ProductService ps = new ProductServiceImpl();
    private OrderService os = new OrderServiceImpl();
    private FlashSaleService flashService = new FlashSaleServiceImpl();
    private CouponService couponService = new CouponServiceImpl();

    // ====== MODULE CON (Khởi tạo lazy khi cần) ======
    private ProductBrowse productBrowse;
    private CartManager cartManager;
    private CheckoutHandler checkoutHandler;
    private OrderHistory orderHistory;

    public CustomerMenu() {
        // Inject dependencies + shared state vào các module con
        this.productBrowse = new ProductBrowse(ps, flashService, cart);
        this.cartManager = new CartManager(cart);
        this.checkoutHandler = new CheckoutHandler(os, flashService, couponService, cart);
        this.orderHistory = new OrderHistory(os);
    }

    public void displayCustomerMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n+-------------------------------------------------------------+");
            System.out.println("|                CỬA HÀNG ĐIỆN THOẠI (CUSTOMER)               |");
            System.out.println("+-------------------------------------------------------------+");
            System.out.println("| Từ khóa lọc: " + (currentSearchQuery == null ? "Không" : "'" + currentSearchQuery + "'") + 
                               " | Trong giỏ: " + cart.size() + " SP                          |");
            System.out.println("+-------------------------------------------------------------+");
            System.out.println("| 1. Xem danh sách sản phẩm & Thêm vào giỏ hàng               |");
            System.out.println("| 2. Đặt từ khóa tìm kiếm sản phẩm                            |");
            System.out.println("| 3. Xem Giỏ hàng & Thanh toán (Sửa/Xóa/Checkout)             |");
            System.out.println("| 4. Lịch sử mua hàng                                         |");
            System.out.println("| 0. Đăng xuất                                                |");
            System.out.println("+-------------------------------------------------------------+");
            System.out.print  ("| Chọn chức năng: ");
            
            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    productBrowse.displayProductsAndShop(sc, currentSearchQuery);
                    break;
                case "2":
                    currentSearchQuery = searchProduct(sc);
                    break;
                case "3":
                    cartManager.manageCartAndCheckout(sc, checkoutHandler);
                    break;
                case "4":
                    orderHistory.viewOrderHistory();
                    break;
                case "0":
                    Util.Session.clear();
                    cart.clear(); // Hủy giỏ hàng
                    System.out.println("Đã đăng xuất tài khoản.");
                    return;
                default:
                    System.err.println("Lựa chọn không hợp lệ, vui lòng thử lại!");
            }
        }
    }

    /**
     * Đặt từ khóa tìm kiếm — trả về keyword mới hoặc null nếu người dùng xóa bộ lọc.
     */
    private String searchProduct(Scanner sc) {
        System.out.print("Nhập tên sản phẩm để tìm kiếm (Enter trống để bỏ tìm kiếm): ");
        String query = sc.nextLine();
        if (query.trim().isEmpty()) {
            System.out.println("Đã xóa bộ lọc tìm kiếm.");
            return null;
        } else {
            System.out.println("Đã lưu. Hãy vào Menu số 1 để xem kết quả.");
            return query;
        }
    }
}
