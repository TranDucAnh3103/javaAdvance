package Presentation;

import Models.CoreEntities.User;
import Models.DTO.CartItem;
import Models.DTO.ProductDTO;
import Models.MarketingPromotions.FlashSaleItem;
import Models.SalesOrders.Order;
import Service.OrderService;
import Service.OrderServiceImpl;
import Service.ProductService;
import Service.ProductServiceImpl;
import Service.FlashSaleService;
import Service.FlashSaleServiceImpl;
import Service.CouponService;
import Service.CouponServiceImpl;
import Models.MarketingPromotions.Coupon;
import Util.Session;
import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Customer {

    private ProductService ps = new ProductServiceImpl();
    private OrderService os = new OrderServiceImpl();
    private FlashSaleService flashService = new FlashSaleServiceImpl();
    private CouponService couponService = new CouponServiceImpl();

    // Giỏ hàng lưu trữ tạm trên RAM trong phiên làm việc
    private List<CartItem> cart = new ArrayList<>();
    
    // Lưu phiên tìm kiếm
    private String currentSearchQuery = null;

    public void displayCustomerMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n+-------------------------------------------------------------+");
            System.out.println("|                CỬA HÀNG ĐIỆN THOẠI (CUSTOMER)               |");
            System.out.println("+-------------------------------------------------------------+");
            System.out.println("| Từ khóa lọc: " + (currentSearchQuery == null ? "Không" : "'" + currentSearchQuery + "'") + 
                               " | Trong giỏ: " + cart.size() + " SP                              |");
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
                    displayProductsAndShop(sc);
                    break;
                case "2":
                    searchProduct(sc);
                    break;
                case "3":
                    manageCartAndCheckout(sc);
                    break;
                case "4":
                    viewOrderHistory();
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

    private void searchProduct(Scanner sc) {
        System.out.print("Nhập tên sản phẩm để tìm kiếm (Enter trống để bỏ tìm kiếm): ");
        String query = sc.nextLine();
        if (query.trim().isEmpty()) {
            this.currentSearchQuery = null;
            System.out.println("Đã xóa bộ lọc tìm kiếm.");
        } else {
            this.currentSearchQuery = query;
            System.out.println("Đã lưu. Hãy vào Menu số 1 để xem kết quả.");
        }
    }

    private void displayProductsAndShop(Scanner sc) {
        int limit = 5; 
        int page = 1;

        while (true) {
            try {
                // Truyền true vào tham số inStockOnly để CHỈ ĐẾM sản phẩm còn hàng
                int totalProducts = ps.getTotalProducts(currentSearchQuery, true);
                if (totalProducts <= 0) {
                    System.out.println("=> Rất tiếc, hiện tại không có sản phẩm nào đang được bày bán.");
                    return;
                }

                int totalPages = (int) Math.ceil((double) totalProducts / limit);
                if (page < 1) page = 1;
                if (page > totalPages) page = totalPages;

                int offset = (page - 1) * limit;

                // Gửi "inStockOnly = true" xuống Services/DAO
                List<ProductDTO> list = ps.getAllProducts(limit, offset, currentSearchQuery, null, null, true);

                // Cập nhật lượng tồn kho ảo (Virtual Stock) dựa trên số máy đang nằm trên Giỏ RAM chờ thanh toán
                for (ProductDTO p : list) {
                    for (CartItem ci : cart) {
                        if (ci.getVariantId() == p.getVariantId()) {
                            // Trừ ảo đi để UI hiển thị số tồn kho = Tồn thật DB - Tồn tạm trong RAM
                            p.setStock(p.getStock() - ci.getQuantity());
                        }
                    }
                }

                // Hiển thị bảng sản phẩm — nếu SP đang Flash Sale sẽ hiện thêm tag [FLASH] và giá đã giảm
                System.out.println("\n" + "=".repeat(145)); 
                System.out.printf("| %-7s | %-15s | %-32s | %-20s | %-15s | %-15s | %-15s | %-6s |%n",
                        "Mã SP", "Danh mục", "Tên sản phẩm", "Cấu hình", "Màu sắc", "Giá gốc", "Giá bán", "Kho");
                System.out.println("-".repeat(145));

                for (ProductDTO p : list) {
                    String specs = p.getRam() + "/" + p.getRom();
                    
                    // Kiểm tra xem variant này có đang tham gia Flash Sale nào không
                    BigDecimal displayPrice = p.getPrice();
                    String priceLabel = String.format("%-15.2f", displayPrice);
                    try {
                        FlashSaleItem flashItem = flashService.getActiveFlashSaleItem(p.getVariantId());
                        if (flashItem != null) {
                            // Có Flash Sale → tính giá mới và đánh tag cho nổi bật
                            displayPrice = flashService.calculateFlashPrice(p.getPrice(), p.getVariantId());
                            priceLabel = String.format("%-10.2f [⚡]", displayPrice);
                        }
                    } catch (DatabaseException ignored) {
                        // Nếu lỗi kiểm tra Flash Sale → cứ hiện giá thường, không crash
                    }
                    
                    System.out.printf("| %-7d | %-15s | %-32s | %-20s | %-15s | %-15.2f | %-15s | %-6d |%n",
                            p.getVariantId(),
                            p.getCategoryName(),
                            p.getProductName(),
                            specs,
                            p.getColor(),
                            p.getPrice(),
                            priceLabel,
                            p.getStock());
                }
                
                System.out.println("=".repeat(145));
                System.out.println("Trang " + page + " / " + totalPages + " (Tổng: " + totalProducts + " thiết bị còn hàng)");
                System.out.println("Ghi chú: [⚡] = Đang Flash Sale, giá bán đã được giảm!");
                
                System.out.println("\nĐiều hướng mượt: [n] Trang sau - [p] Trang trước - [số trang] Đi đến trang");
                System.out.println("Lệnh mua hàng    : [b Mã_SP] (Ví dụ: 'b 12' để bỏ mã 12 vào Giỏ)");
                System.out.println("Thoát danh sách  : [0]");
                System.out.print("=> Nhập lệnh: ");

                String cmd = sc.nextLine().trim().toLowerCase();
                
                // MUA HÀNG: Cú pháp 'b khoảng_trắng Mã_SP'
                if (cmd.startsWith("b ")) {
                    try {
                        int vId = Integer.parseInt(cmd.substring(2).trim()); // Lấy mã số
                        addToCart(sc, vId, list); // Hàm bốc hàng ném vào List RAM
                    } catch (NumberFormatException e) {
                        System.err.println("=> Mã sản phẩm không hợp lệ!");
                    }
                } 
                else if (cmd.equals("0")) {
                    break;
                } else if (cmd.equals("n")) {
                    if (page < totalPages) page++;
                    else System.out.println("=> Đã ở trang cuối!");
                } else if (cmd.equals("p")) {
                    if (page > 1) page--;
                    else System.out.println("=> Đã ở trang đầu!");
                } else {
                    try {
                        int pStr = Integer.parseInt(cmd);
                        if (pStr >= 1 && pStr <= totalPages) page = pStr;
                        else System.out.println("=> Trang không hợp lệ!");
                    } catch (NumberFormatException e) {
                        System.out.println("=> Lệnh không nhận diện được!");
                    }
                }
            } catch (DatabaseException e) {
                System.err.println("Lỗi hệ thống hiển thị: " + e.getMessage());
                break;
            }
        }
    }

    /**
     * Logic nạp sản phẩm từ bảng hiển thị vào Giỏ hàng
     */
    private void addToCart(Scanner sc, int variantId, List<ProductDTO> displayList) {
        ProductDTO selectedDTO = null;
        for (ProductDTO p : displayList) {
            if (p.getVariantId() == variantId) {
                selectedDTO = p;
                break;
            }
        }

        if (selectedDTO == null) {
            System.err.println("=> Không tìm thấy Mã SP [" + variantId + "] trong trang hiện tại.");
            return;
        }

        System.out.print("=> Nhập số lượng muốn mua cho SP [" + selectedDTO.getProductName() + "]: ");
        try {
            int qty = Integer.parseInt(sc.nextLine());
            if (qty <= 0) {
                System.err.println("=> Số lượng mua phải > 0!");
                return;
            }
            
            CartItem existingItem = null;
            for (CartItem ci : cart) {
                if (ci.getVariantId() == variantId) {
                    existingItem = ci;
                    break;
                }
            }
            
            // (VIRTUAL STOCK MAPPING)
            // Tồn kho ở màn hình UI lúc này (selectedDTO.getStock()) ĐÃ LÀ TỒN KHO THẬT TRỪ ĐI HÀNG ĐANG CÓ TRONG GIỎ (RAM).
            // Do đó kho ảo còn bao nhiêu thì chỉ được nạp CHỨA tối đa bấy nhiêu.
            if (qty > selectedDTO.getStock()) {
                System.err.println("=> [LỖI KHO] Số tồn kho khả dụng hiện chỉ còn " + selectedDTO.getStock() + " máy (Đã trừ hao số máy đang có trong Giỏ)! Việc nhập thêm " + qty + " máy lỗi.");
                return;
            }

            // Mọi điều kiện hợp lệ -> Thêm vào List
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + qty);
                System.out.println("=> Cập nhật số lượng gộp chung trong giỏ thành công: " + existingItem.getQuantity() + " máy.");
            } else {
                String fullSpecs = selectedDTO.getRam() + "/" + selectedDTO.getRom() + " - " + selectedDTO.getColor();
                
                // Xác định giá đút vào giỏ: Nếu đang Flash Sale → dùng giá flash, không thì giá gốc.
                // Giá này sẽ được ghi cứng vào order_details (unit_price) khi checkout.
                BigDecimal cartPrice = selectedDTO.getPrice();
                try {
                    BigDecimal flashPrice = flashService.calculateFlashPrice(selectedDTO.getPrice(), selectedDTO.getVariantId());
                    if (flashPrice.compareTo(selectedDTO.getPrice()) < 0) {
                        cartPrice = flashPrice;
                        System.out.println("   [⚡ FLASH SALE] Giá gốc: " + selectedDTO.getPrice() + " → Giá Flash: " + flashPrice);
                    }
                } catch (DatabaseException ignored) {}
                
                CartItem newItem = new CartItem(
                        selectedDTO.getVariantId(),
                        selectedDTO.getProductId(),
                        selectedDTO.getProductName(),
                        fullSpecs,
                        cartPrice,
                        qty
                );
                cart.add(newItem);
                System.out.println("=> Đã thêm [" + selectedDTO.getProductName() + "] vào Giỏ hàng ! ");
            }

        } catch (NumberFormatException e) {
            System.err.println("=> Số lượng không hợp lệ!");
        }
    }


    //case 3
    private void manageCartAndCheckout(Scanner sc) {
        while (true) {
            if (cart.isEmpty()) {
                System.out.println("\n[GIỎ HÀNG] Giỏ phần cứng đang trống xốp!");
                return;
            }

            System.out.println("\n" + "=".repeat(100));
            System.out.println("                           GIỎ HÀNG HIỆN TẠI                          ");
            System.out.println("-".repeat(100));
            System.out.printf("| %-7s | %-30s | %-20s | %-10s | %-16s |%n", "Mã SP", "Tên Sản Phẩm", "Thông Số", "Số lượng", "Thành tiền (VNĐ)");
            System.out.println("-".repeat(100));

            BigDecimal cartTotal = BigDecimal.ZERO;
            for (CartItem ci : cart) {
                BigDecimal lineTotal = ci.getTotalPrice();
                cartTotal = cartTotal.add(lineTotal);
                System.out.printf("| %-7d | %-30s | %-20s | %-10d | %-16.2f |%n", 
                        ci.getVariantId(), ci.getProductName(), ci.getSpecs(), ci.getQuantity(), lineTotal);
            }
            System.out.println("=".repeat(100));
            System.out.printf(" TỔNG DANH MỤC: %-5d  | TỔNG TIỀN THANH TOÁN GÓI GỌN: %.2f VNĐ%n", cart.size(), cartTotal);
            
            System.out.println("\n[CÁC LỆNH TƯƠNG TÁC GIỎ HÀNG]");
            System.out.println("  1. Tới Quầy Thanh Toán (Checkout)");
            System.out.println("  2. Sửa số lượng của mặt hàng (Nhập mã)");
            System.out.println("  3. XÓA mặt hàng khỏi giỏ (Nhập mã)");
            System.out.println("  4. HỦY TOÀN BỘ giỏ hàng (xóa tất cả sản phẩm giỏ hàng)");
            System.out.println("  0. Quay lại Menu chính (Cất giỏ đó để mua tiếp)");
            System.out.print(" => Xin chọn: ");
            
            String cText = sc.nextLine();
            switch (cText) {
                case "1":
                    handleCheckout(sc);
                    return; // Thanh toán xong rồi thì sút về menu chính.
                case "2":
                    editCartQuantity(sc);
                    break;
                case "3":
                    removeCartItem(sc);
                    break;
                case "4":
                    cart.clear();
                    System.out.println("=> Đã clear hoàn toàn giỏ hàng !");
                    return;
                case "0":
                    return;
                default:
                    System.out.println("=> Lệnh sai!");
            }
        }
    }

    private void editCartQuantity(Scanner sc) {
        System.out.print("=> Nhập Mã SP (Variant ID) mục muốn sửa số lượng: ");
        try {
            int vId = Integer.parseInt(sc.nextLine());
            CartItem target = null;
            for (CartItem ci : cart) {
                if (ci.getVariantId() == vId) {
                    target = ci;
                    break;
                }
            }
            if (target == null) {
                System.err.println("=> Không tìm thấy Mã SP này nằm trong Giỏ.");
                return;
            }

            System.out.print("=> Nhập số lượng MỚI cho [" + target.getProductName() + "]: ");
            int newQty = Integer.parseInt(sc.nextLine());
            
            if (newQty <= 0) {
                cart.remove(target);
                System.out.println("=> Do gõ số lượng = 0, món hàng bị rớt khỏi giỏ luôn! ");
            } else {
                target.setQuantity(newQty);
                System.out.println("=> Đã thêm " + newQty + " máy trong giỏ hàng!");
            }
        } catch (NumberFormatException e) {
            System.err.println("=> Mã SP hoắc Số Lượng nhập vào lỗi!");
        }
    }

    private void removeCartItem(Scanner sc) {
        System.out.print("=> Nhập Mã SP (Variant ID) mục muốn xóa khỏi giỏ: ");
        try {
            int vId = Integer.parseInt(sc.nextLine());
            CartItem target = null;
            for (CartItem ci : cart) {
                if (ci.getVariantId() == vId) {
                    target = ci;
                    break;
                }
            }
            if (target != null) {
                cart.remove(target);
                System.out.println("=> Đã xóa [" + target.getProductName() + "] ra khỏi giỏ hàng.");
            } else {
                System.err.println("=> Tìm không ra mã SP trên trong Giỏ.");
            }
        } catch (NumberFormatException e) {
            System.err.println("=> Mã SP chỉ được chứa chữ số nguyên!");
        }
    }

    // Nghiệp vụ Thanh Toán — Giờ có thêm bước hỏi mã giảm giá (Coupon) trước khi chốt đơn.
    private void handleCheckout(Scanner sc) {
        User u = Session.getLoggedInUser();
        System.out.println("\n+-------------------------------------------------------------+");
        System.out.println("|                   XÁC NHẬN THANH TOÁN                       |");
        System.out.println("+-------------------------------------------------------------+");
        
        String shippingAddr = u.getAddress();
        
        if (shippingAddr == null || shippingAddr.trim().isEmpty()) {
            System.out.println("=> Rất tiếc, Hồ sơ bạn chưa ghi định vị chỗ ở.");
            System.out.print("=> Mời gõ địa chỉ Giao hàng ngay bây giờ: ");
            shippingAddr = sc.nextLine();
        } else {
            System.out.println("=> Địa chỉ nhận diện được: " + shippingAddr);
            System.out.print("=> Ấn [Enter] để dùng luôn định vị này, hoặc ấn [e] để Sửa: ");
            String confirm = sc.nextLine().trim().toLowerCase();
            if (confirm.equals("e")) {
                System.out.print("=> Khai báo địa chỉ mới: ");
                shippingAddr = sc.nextLine();
            }
        }

        // ====== BƯỚC MỚI: HỎi mã giảm giá (Coupon) ======
        // Khách có thể nhập mã hoặc Enter bỏ qua. Nếu nhập mã sai thì báo lỗi nhưng KHÔNG hủy checkout.
        Coupon appliedCoupon = null;
        System.out.print("=> Bạn có mã giảm giá? Nhập mã hoặc [Enter] để bỏ qua: ");
        String couponCode = sc.nextLine().trim();
        if (!couponCode.isEmpty()) {
            try {
                appliedCoupon = couponService.validateAndGetCoupon(couponCode);
                System.out.println("   [✅ COUPON HỢP LỆ] Mã '" + appliedCoupon.getCode() + "' giảm " + appliedCoupon.getDiscountPercent() + "% trên tổng đơn!");
            } catch (InvalidInputException e) {
                System.err.println("   [❌] " + e.getMessage() + " — Tiếp tục thanh toán không giảm giá.");
            } catch (DatabaseException e) {
                System.err.println("   [LỖI HỆ THỐNG] " + e.getMessage());
            }
        }

        // Tính tổng tiền hiện tại và hiển thị cho khách trước khi chốt
        BigDecimal cartTotal = BigDecimal.ZERO;
        for (CartItem ci : cart) {
            cartTotal = cartTotal.add(ci.getTotalPrice());
        }
        System.out.println("\n   Tổng tiền giỏ hàng: " + String.format("%,.2f", cartTotal) + " VNĐ");

        BigDecimal finalTotal = cartTotal;
        if (appliedCoupon != null) {
            finalTotal = couponService.applyCouponDiscount(cartTotal, appliedCoupon);
            System.out.println("   Giảm coupon (" + appliedCoupon.getDiscountPercent() + "%): -" + String.format("%,.2f", cartTotal.subtract(finalTotal)) + " VNĐ");
            System.out.println("   ===> THÀNH TOÁN CUỐI: " + String.format("%,.2f", finalTotal) + " VNĐ");
        }

        System.out.print("=> Bấm chữ [Y] để chốt đơn, bất kì phím nào khác để HỦY: ");
        String finalOk = sc.nextLine().trim();

        if (finalOk.equalsIgnoreCase("Y")) {
            System.out.println("\n...Hệ thống đang xử lý...");
            try {
                // Gởi checkout với tổng tiền đã trừ coupon (nếu có)
                boolean success = os.checkout(u.getId(), shippingAddr, cart);
                if (success) {
                    // Nếu có coupon → trừ lượt dùng trong DB
                    if (appliedCoupon != null) {
                        try {
                            couponService.deductUsage(appliedCoupon.getId());
                        } catch (DatabaseException e) {
                            // Lỗi trừ lượt coupon không nên hủy đơn hàng (đơn đã chốt rồi)
                            System.err.println("[CẢNH BÁO] Không thể trừ lượt coupon: " + e.getMessage());
                        }
                    }
                    System.out.println("\n[XUẤT BILL THÀNH CÔNG] Tiền đã trao - cháo đã múc, xin cám ơn Quý Khách!");
                    cart.clear();
                }
            } catch (InvalidInputException e) {
                System.err.println("\n[LỖI GIAO DỊCH] " + e.getMessage());
            } catch (DatabaseException e) {
                System.err.println("\n[HỆ THỐNG LỖI] " + e.getMessage());
            }
        } else {
            System.out.println("=> Đã thoát khỏi Quầy thanh toán.");
        }
    }

    private void viewOrderHistory() {
        System.out.println("\n--- LỊCH SỬ THANH TOÁN ---");
        try {
            User u = Session.getLoggedInUser();
            List<Order> orders = os.getOrderHistory(u.getId());
            
            if (orders.isEmpty()) {
                System.out.println("=> Bạn chưa từng mua thứ gì.");
                return;
            }

            System.out.println("=".repeat(120));
            System.out.printf("| %-10s | %-20s | %-15s | %-15s | %-40s |%n", 
                    "Mã ĐH", "Ngày Mua", "Trạng Thái", "Tổng Bill (VNĐ)", "Địa chỉ");
            System.out.println("-".repeat(120));
            
            for (Order o : orders) {
                System.out.printf("| %-10d | %-20s | %-15s | %-15.2f | %-40s |%n",
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
