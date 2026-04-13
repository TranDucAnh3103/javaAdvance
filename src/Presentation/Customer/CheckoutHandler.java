package Presentation.Customer;

import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;
import Models.CoreEntities.User;
import Models.DTO.CartItem;
import Models.MarketingPromotions.Coupon;
import Service.Promotion.CouponService;
import Service.Promotion.FlashSaleService;
import Service.Sales.OrderService;
import Util.Session;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Module xử lý thanh toán (Checkout) cho Customer.
 * Bao gồm: nhập địa chỉ, nhập mã giảm giá (Coupon), xác nhận đặt hàng.
 *
 * Tách từ Customer.java gốc: handleCheckout()
 */
public class CheckoutHandler {

    private OrderService os;
    private FlashSaleService flashService;
    private CouponService couponService;
    private List<CartItem> cart;

    public CheckoutHandler(OrderService os, FlashSaleService flashService, CouponService couponService, List<CartItem> cart) {
        this.os = os;
        this.flashService = flashService;
        this.couponService = couponService;
        this.cart = cart;
    }

    /**
     * Nghiệp vụ Thanh Toán — có bước hỏi mã giảm giá (Coupon) trước khi chốt đơn.
     */
    public void handleCheckout(Scanner sc) {
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
                System.out.println("   [ COUPON HỢP LỆ] Mã '" + appliedCoupon.getCode() + "' giảm " + appliedCoupon.getDiscountPercent() + "% trên tổng đơn!");
            } catch (InvalidInputException e) {
                System.err.println("    " + e.getMessage() + " — Tiếp tục thanh toán không giảm giá.");
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
}
