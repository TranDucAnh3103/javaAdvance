package Presentation.Customer;

import Exceptions.DatabaseException;
import Models.DTO.CartItem;
import Models.DTO.ProductDTO;
import Models.MarketingPromotions.FlashSaleItem;
import Service.Promotion.FlashSaleService;
import Service.Product.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Module duyệt sản phẩm cho Customer.
 * Hiển thị danh sách SP còn hàng (có phân trang) và cho phép thêm vào giỏ.
 * 
 * Tách từ Customer.java gốc: displayProductsAndShop() + addToCart()
 */
public class ProductBrowse {

    private ProductService ps;
    private FlashSaleService flashService;
    private List<CartItem> cart; // Tham chiếu trực tiếp đến giỏ hàng trên RAM của CustomerMenu

    public ProductBrowse(ProductService ps, FlashSaleService flashService, List<CartItem> cart) {
        this.ps = ps;
        this.flashService = flashService;
        this.cart = cart;
    }

    /**
     * Hiển thị danh sách SP còn hàng, hỗ trợ phân trang và nhập lệnh mua hàng.
     * @param sc Scanner dùng chung
     * @param currentSearchQuery từ khóa tìm kiếm hiện tại (null = không lọc)
     */
    public void displayProductsAndShop(Scanner sc, String currentSearchQuery) {
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
                    
                    System.out.printf("| %-7d | %-15.15s | %-32.32s | %-20.20s | %-15.15s | %-15.2f | %-15.15s | %-6d |%n",
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
}
