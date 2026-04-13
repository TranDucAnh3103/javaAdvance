package Presentation.Admin;

import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;
import Models.MarketingPromotions.Coupon;
import Service.Promotion.CouponService;
import Service.Promotion.CouponServiceImpl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Giao diện quản lý Mã giảm giá (Coupon) cho Admin.
 * Admin tạo coupon (chọn code, % giảm, thời hạn, số lượt) và xem danh sách.
 * Customer nhập mã coupon lúc checkout — logic đó nằm bên CheckoutHandler.java.
 */
public class CouponPresentation {

    private CouponService couponService = new CouponServiceImpl();
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void displayMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n+--------------------------------------------------+");
            System.out.println("|          QUẢN LÝ MÃ GIẢM GIÁ (ADMIN)             |");
            System.out.println("+--------------------------------------------------+");
            System.out.println("|  1. Tạo mã giảm giá mới                         |");
            System.out.println("|  2. Xem danh sách mã giảm giá                   |");
            System.out.println("|  0. Quay lại menu chính                          |");
            System.out.println("+--------------------------------------------------+");
            System.out.print("|  Chọn chức năng: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    createCoupon(sc);
                    break;
                case "2":
                    viewAllCoupons();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    /**
     * Tạo mã giảm giá mới.
     * Mã code nên viết HOA và không chứa dấu cách — VD: SALE10, TETAM2025, FLASHFRIDAY.
     */
    private void createCoupon(Scanner sc) {
        System.out.println("\n--- TẠO MÃ GIẢM GIÁ MỚI ---");

        System.out.print("Nhập mã code (VD: SALE10, TETAM2025): ");
        String code = sc.nextLine().trim().toUpperCase(); // Ép viết hoa cho đồng nhất

        System.out.print("Phần trăm giảm giá (VD: 10 nghĩa là giảm 10%): ");
        BigDecimal discountPercent;
        try {
            discountPercent = new BigDecimal(sc.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("=> Lỗi: Phần trăm phải là số!");
            return;
        }

        System.out.println("Nhập thời gian theo định dạng: yyyy-MM-dd HH:mm");
        System.out.print("Ngày bắt đầu hiệu lực: ");
        String fromStr = sc.nextLine();
        System.out.print("Ngày hết hiệu lực: ");
        String toStr = sc.nextLine();

        System.out.print("Số lượt sử dụng tối đa: ");
        int usageLimit;
        try {
            usageLimit = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("=> Lỗi: Số lượt phải là số nguyên!");
            return;
        }

        try {
            Timestamp validFrom = Timestamp.valueOf(LocalDateTime.parse(fromStr, DTF));
            Timestamp validTo = Timestamp.valueOf(LocalDateTime.parse(toStr, DTF));

            Coupon coupon = new Coupon();
            coupon.setCode(code);
            coupon.setDiscountPercent(discountPercent);
            coupon.setValidFrom(validFrom);
            coupon.setValidTo(validTo);
            coupon.setUsageLimit(usageLimit);
            coupon.setActive(true);

            if (couponService.createCoupon(coupon)) {
                System.out.println("=> Tạo thành công! Mã [" + code + "] giảm " + discountPercent + "%, dùng được " + usageLimit + " lần.");
            }

        } catch (DateTimeParseException e) {
            System.err.println("=> Lỗi: Định dạng thời gian sai! Phải đúng kiểu: yyyy-MM-dd HH:mm");
        } catch (InvalidInputException e) {
            System.err.println("=> Lỗi validate: " + e.getMessage());
        } catch (DatabaseException e) {
            System.err.println("=> Lỗi hệ thống: " + e.getMessage());
        }
    }

    private void viewAllCoupons() {
        System.out.println("\n--- DANH SÁCH TẤT CẢ MÃ GIẢM GIÁ ---");
        try {
            List<Coupon> coupons = couponService.getAllCoupons();
            if (coupons.isEmpty()) {
                System.out.println("=> Chưa có mã giảm giá nào.");
                return;
            }

            System.out.println("=".repeat(120));
            System.out.printf("| %-5s | %-15s | %-8s | %-20s | %-20s | %-8s | %-12s |%n",
                    "ID", "Mã code", "Giảm(%)", "Hiệu lực từ", "Đến", "Còn lượt", "Trạng thái");
            System.out.println("-".repeat(120));

            Timestamp now = new Timestamp(System.currentTimeMillis());
            for (Coupon c : coupons) {
                // Xác định trạng thái hiển thị cho Admin dễ nhìn
                String statusLabel;
                if (!c.isActive()) {
                    statusLabel = "TẮT";
                } else if (c.getUsageLimit() <= 0) {
                    statusLabel = "HẾT LƯỢT";
                } else if (now.before(c.getValidFrom())) {
                    statusLabel = "CHƯA ĐẾN HẠN";
                } else if (now.after(c.getValidTo())) {
                    statusLabel = "HẾT HẠN";
                } else {
                    statusLabel = "ĐANG DÙNG ★";
                }

                System.out.printf("| %-5d | %-15s | %-8s | %-20s | %-20s | %-8d | %-12s |%n",
                        c.getId(),
                        c.getCode(),
                        c.getDiscountPercent().toString() + "%",
                        c.getValidFrom().toString(),
                        c.getValidTo().toString(),
                        c.getUsageLimit(),
                        statusLabel);
            }
            System.out.println("=".repeat(120));

        } catch (DatabaseException e) {
            System.err.println("=> Lỗi hệ thống: " + e.getMessage());
        }
    }
}
