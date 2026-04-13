package Presentation.Customer;

import Models.DTO.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Module quản lý giỏ hàng cho Customer.
 * Hiển thị giỏ, sửa số lượng, xóa SP, hủy giỏ.
 * Khi Customer chọn Checkout → delegate sang CheckoutHandler.
 *
 * Tách từ Customer.java gốc: manageCartAndCheckout(), editCartQuantity(), removeCartItem()
 */
public class CartManager {

    private List<CartItem> cart; // Tham chiếu đến giỏ hàng dùng chung từ CustomerMenu

    public CartManager(List<CartItem> cart) {
        this.cart = cart;
    }

    /**
     * Menu quản lý giỏ hàng: xem, sửa, xóa, checkout.
     * @param sc Scanner dùng chung
     * @param checkoutHandler module xử lý thanh toán (truyền vào để gọi khi customer chọn checkout)
     */
    public void manageCartAndCheckout(Scanner sc, CheckoutHandler checkoutHandler) {
        while (true) {
            if (cart.isEmpty()) {
                System.out.println("\n[GIỎ HÀNG] đang trống!");
                return;
            }

            System.out.println("\n" + "=".repeat(100));
            System.out.println("                           GIỎ HÀNG HIỆN TẠI                          ");
            System.out.println("-".repeat(100));
            System.out.printf("| %-7.7s | %-30.30s | %-20.20s | %-10.10s | %-16.16s |%n", "Mã SP", "Tên Sản Phẩm", "Thông Số", "Số lượng", "Thành tiền (VNĐ)");
            System.out.println("-".repeat(100));

            BigDecimal cartTotal = BigDecimal.ZERO;
            for (CartItem ci : cart) {
                BigDecimal lineTotal = ci.getTotalPrice();
                cartTotal = cartTotal.add(lineTotal);
                System.out.printf("| %-7d | %-30.30s | %-20.20s | %-10d | %-16.2f |%n", 
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
                    checkoutHandler.handleCheckout(sc);
                    return; // Thanh toán xong rồi thì về menu chính.
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
}
