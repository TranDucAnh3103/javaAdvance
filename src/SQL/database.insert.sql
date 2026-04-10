/* * DATA SEEDING FOR SMARTPHONE STORE (EXCLUDING users TABLE)
 * Project: PRJ-PHONE-STORE-JAVA-02
 */

-- 1. INSERT CATEGORIES
-- Dữ liệu mẫu cho phân loại sản phẩm
INSERT INTO categories (name) VALUES
                                  ('Smartphone'),
                                  ('Tablet'),
                                  ('Accessories'),
                                  ('Smartwatch');

-- 2. INSERT PRODUCTS
-- Lưu ý: ID được giả định tự tăng từ 1
INSERT INTO products (category_id, name, brand, description) VALUES
                                                                 (1, 'iPhone 15 Pro Max', 'Apple', 'Flagship mới nhất từ Apple với khung Titan.'),
                                                                 (1, 'Samsung Galaxy S24 Ultra', 'Samsung', 'Điện thoại tích hợp AI quyền năng.'),
                                                                 (2, 'iPad Air M2', 'Apple', 'Hiệu năng mạnh mẽ với chip M2.'),
                                                                 (3, 'AirPods Pro Gen 2', 'Apple', 'Chống ồn chủ động vượt trội.');

-- 3. INSERT PRODUCT VARIANTS
-- Cấu hình chi tiết RAM/ROM và Giá
INSERT INTO product_variants (product_id, ram, rom, color, price, stock) VALUES
                                                                             (1, '8GB', '256GB', 'Natural Titanium', 29990000.00, 50),
                                                                             (1, '8GB', '512GB', 'Blue Titanium', 34990000.00, 30),
                                                                             (2, '12GB', '256GB', 'Titanium Black', 26000000.00, 40),
                                                                             (3, '8GB', '128GB', 'Space Gray', 16000000.00, 20),
                                                                             (4, NULL, NULL, 'White', 5000000.00, 100);

-- 4. INSERT COUPONS
-- Thiết lập mã giảm giá (Lưu ý: valid_to nên là tương lai để bạn test được)
INSERT INTO coupons (code, discount_percent, valid_from, valid_to, usage_limit) VALUES
                                                                                    ('HELLOSUMMER', 10.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100),
                                                                                    ('VIPSTORE', 20.00, '2026-04-01 00:00:00', '2026-05-01 00:00:00', 50),
                                                                                    ('FLASH15', 15.00, '2026-04-09 00:00:00', '2026-04-10 00:00:00', 10);

-- 5. INSERT FLASH SALES
-- Tạo khung giờ sự kiện Flash Sale
INSERT INTO flash_sales (name, start_time, end_time) VALUES
                                                         ('Midnight Sale', '2026-04-10 00:00:00', '2026-04-10 02:00:00'),
                                                         ('Weekend Warrior', '2026-04-12 09:00:00', '2026-04-12 21:00:00');

-- 6. INSERT FLASH SALE ITEMS
-- Gán biến thể sản phẩm vào sự kiện Flash Sale
-- Giảm iPhone 15 Pro Max 256GB (variant_id 1) 12% trong Midnight Sale
INSERT INTO flash_sale_items (flash_sale_id, variant_id, discount_percent, sale_stock) VALUES
                                                                                           (1, 1, 12.00, 5),
                                                                                           (1, 3, 15.00, 10),
                                                                                           (2, 4, 10.00, 5);

-- 7. INSERT ORDERS (Giả định user_id = 1 đã tồn tại trong DB)
-- Lưu ý: Chạy cái này sau khi bạn đã tạo thủ công 1 user trong bảng users
 INSERT INTO orders (user_id, coupon_id, total_amount, status, shipping_address) VALUES
 (1, 1, 26991000.00, 'DELIVERED', '123 Nguyen Trai, Q1, HCM');

-- 8. INSERT ORDER DETAILS
-- Chi tiết hóa đơn cho order trên
 INSERT INTO order_details (order_id, variant_id, quantity, unit_price) VALUES
(1, 1, 1, 29990000.00);