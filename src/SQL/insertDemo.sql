/*
 * SCRIPT RESET VÀ SEEDING DỮ LIỆU DEMO 2026 - HỆ THỐNG SMARTPHONE STORE
 * Mục tiêu: Đảm bảo toàn bộ mốc thời gian nằm trong năm 2026.
 * Dự án: PRJ-PHONE-STORE-JAVA-02
 */

SET FOREIGN_KEY_CHECKS = 0;

-- 1. RESET DỮ LIỆU
TRUNCATE TABLE flash_sale_items;
TRUNCATE TABLE flash_sales;
TRUNCATE TABLE order_details;
DELETE FROM orders;
ALTER TABLE orders AUTO_INCREMENT = 1;
DELETE FROM coupons;
ALTER TABLE coupons AUTO_INCREMENT = 1;
DELETE FROM product_variants;
ALTER TABLE product_variants AUTO_INCREMENT = 1;
DELETE FROM products;
ALTER TABLE products AUTO_INCREMENT = 1;
DELETE FROM categories;
ALTER TABLE categories AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- =======================================================================
-- 2. INSERT CATEGORIES
-- =======================================================================
INSERT INTO categories (id, name, is_deleted) VALUES
                                                  (1, 'Smartphone', FALSE), (2, 'Tablet', FALSE), (3, 'Laptop', FALSE),
                                                  (4, 'Smartwatch', FALSE), (5, 'Accessories', FALSE);

-- =======================================================================
-- 3. INSERT PRODUCTS (20 sản phẩm)
-- =======================================================================
INSERT INTO products (id, category_id, name, brand, description) VALUES
                                                                     (1, 1, 'iPhone 15 Pro Max', 'Apple', 'Khung Titan, chip A17 Pro'),
                                                                     (2, 1, 'Samsung Galaxy S24 Ultra', 'Samsung', 'Tích hợp bút S-Pen và Galaxy AI'),
                                                                     (3, 1, 'Google Pixel 8 Pro', 'Google', 'Camera AI thuần Google'),
                                                                     (4, 1, 'Xiaomi 14 Ultra', 'Xiaomi', 'Ống kính Leica cao cấp'),
                                                                     (5, 1, 'Oppo Find X7 Ultra', 'Oppo', 'Camera Hasselblad kép'),
                                                                     (6, 2, 'iPad Pro M4', 'Apple', 'Màn hình OLED cực mỏng'),
                                                                     (7, 2, 'Samsung Galaxy Tab S9 Ultra', 'Samsung', 'Máy tính bảng Android lớn nhất'),
                                                                     (8, 2, 'Xiaomi Pad 6 Pro', 'Xiaomi', 'Hiệu năng cao giá hợp lý'),
                                                                     (9, 3, 'MacBook Air M3', 'Apple', 'Mỏng nhẹ, pin cực trâu'),
                                                                     (10, 3, 'Dell XPS 13 Plus', 'Dell', 'Thiết kế tương lai, màn hình vô cực'),
                                                                     (11, 3, 'Asus ROG Zephyrus G14', 'Asus', 'Laptop gaming mạnh mẽ mỏng nhẹ'),
                                                                     (12, 3, 'Lenovo Legion 5', 'Lenovo', 'Laptop gaming quốc dân'),
                                                                     (13, 3, 'HP Spectre x360', 'HP', 'Xoay gập 360 độ linh hoạt'),
                                                                     (14, 4, 'Apple Watch Ultra 2', 'Apple', 'Đồng hồ thể thao chuyên nghiệp'),
                                                                     (15, 4, 'Samsung Galaxy Watch 6', 'Samsung', 'Thiết kế sang trọng, WearOS'),
                                                                     (16, 4, 'Garmin Fenix 7', 'Garmin', 'Pin năng lượng mặt trời'),
                                                                     (17, 5, 'AirPods Pro 2', 'Apple', 'Chống ồn chủ động vượt trội'),
                                                                     (18, 5, 'Samsung Buds 3 Pro', 'Samsung', 'Âm thanh Hi-Fi chất lượng'),
                                                                     (19, 5, 'Sạc dự phòng Anker 20000mAh', 'Anker', 'Sạc nhanh PD 65W'),
                                                                     (20, 5, 'Chuột Logitech MX Master 3S', 'Logitech', 'Chuột công việc tốt nhất');

-- =======================================================================
-- 4. INSERT PRODUCT VARIANTS (21 biến thể)
-- =======================================================================
INSERT INTO product_variants (product_id, ram, rom, color, price, stock) VALUES
                                                                             (1, '8GB', '256GB', 'Natural Titanium', 29000000, 50),
                                                                             (1, '8GB', '512GB', 'Black Titanium', 34000000, 20),
                                                                             (2, '12GB', '256GB', 'Titanium Gray', 26000000, 30),
                                                                             (2, '12GB', '512GB', 'Titanium Black', 31000000, 15),
                                                                             (3, '12GB', '128GB', 'Bay', 18000000, 10),
                                                                             (6, '8GB', '256GB', 'Silver', 28500000, 25),
                                                                             (9, '16GB', '256GB', 'Starlight', 32000000, 15),
                                                                             (9, '16GB', '512GB', 'Midnight', 37000000, 10),
                                                                             (10, '16GB', '512GB', 'Graphite', 42000000, 5),
                                                                             (11, '32GB', '1TB', 'White', 48000000, 3),
                                                                             (14, 'N/A', 'N/A', 'Orange', 21000000, 12),
                                                                             (17, 'N/A', 'N/A', 'White', 5500000, 100),
                                                                             (4, '12GB', '256GB', 'Black', 22000000, 8),
                                                                             (5, '16GB', '512GB', 'Blue', 24000000, 5),
                                                                             (7, '12GB', '256GB', 'Gray', 20000000, 10),
                                                                             (8, '8GB', '128GB', 'Gold', 8500000, 40),
                                                                             (12, '16GB', '512GB', 'Grey', 27000000, 20),
                                                                             (13, '16GB', '1TB', 'Navy', 35000000, 4),
                                                                             (15, 'N/A', 'N/A', 'Silver', 6500000, 30),
                                                                             (19, 'N/A', 'N/A', 'Black', 1500000, 60),
                                                                             (20, 'N/A', 'N/A', 'Pale Gray', 2500000, 45);

-- =======================================================================
-- 5. INSERT COUPONS (20 dòng - Thời gian hoàn toàn trong 2026)
-- =======================================================================
INSERT INTO coupons (code, discount_percent, valid_from, valid_to, usage_limit) VALUES
                                                                                    ('WELCOME2026', 10, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 500),
                                                                                    ('SIEUCAP50', 50, '2026-04-10 00:00:00', '2026-04-20 23:59:59', 2),
                                                                                    ('EXPIRED_JAN', 15, '2026-01-01 00:00:00', '2026-01-31 23:59:59', 100), -- Đã hết hạn
                                                                                    ('SUMMER2026', 15, '2026-06-01 00:00:00', '2026-08-31 23:59:59', 200),
                                                                                    ('TECHDAY', 20, '2026-04-13 00:00:00', '2026-04-15 23:59:59', 50),
                                                                                    ('VIPSTORE', 30, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 10),
                                                                                    ('NEWPHONE', 5, '2026-04-01 00:00:00', '2026-05-01 23:59:59', 1000),
                                                                                    ('APPLEFANS', 12, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 50),
                                                                                    ('SAMSUNG24', 10, '2026-01-01 00:00:00', '2026-06-30 23:59:59', 100),
                                                                                    ('LAPTOPSALE', 15, '2026-04-10 00:00:00', '2026-04-30 23:59:59', 30),
                                                                                    ('GAMING7', 7, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 500),
                                                                                    ('STUDENT', 12, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200),
                                                                                    ('FREESHIP', 5, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 9999),
                                                                                    ('LUCKY99', 99, '2026-04-13 00:00:00', '2026-04-13 23:59:59', 1),
                                                                                    ('BLACKFRIDAY', 40, '2026-11-20 00:00:00', '2026-11-30 23:59:59', 100),
                                                                                    ('XIAOMI10', 10, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200),
                                                                                    ('WATCH5', 5, '2026-04-01 00:00:00', '2026-04-30 23:59:59', 300),
                                                                                    ('GIAM200K', 2, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1000),
                                                                                    ('CUOITUAN', 15, '2026-04-18 00:00:00', '2026-04-20 23:59:59', 50),
                                                                                    ('DONE2026', 20, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100);

-- =======================================================================
-- 6. INSERT FLASH SALES (20 dòng trong năm 2026)
-- =======================================================================
INSERT INTO flash_sales (name, start_time, end_time) VALUES
                                                         ('Siêu Sale Đầu Năm', '2026-01-01 00:00:00', '2026-01-02 00:00:00'),
                                                         ('Valentine Tech', '2026-02-14 00:00:00', '2026-02-14 23:59:59'),
                                                         ('8 Tháng 3 Sale', '2026-03-08 00:00:00', '2026-03-08 23:59:59'),
                                                         ('Đại Tiệc Smartphone', '2026-04-13 09:00:00', '2026-04-14 23:59:59'), -- Đang diễn ra
                                                         ('Giờ Vàng Laptop', '2026-04-15 08:00:00', '2026-04-15 22:00:00'),
                                                         ('Flash Sale 1', '2026-05-15 00:00:00', '2026-05-15 23:59:59'),
                                                         ('Flash Sale 2', '2026-06-16 00:00:00', '2026-06-16 23:59:59'),
                                                         ('Flash Sale 3', '2026-07-17 00:00:00', '2026-07-17 23:59:59'),
                                                         ('Flash Sale 4', '2026-08-18 00:00:00', '2026-08-18 23:59:59'),
                                                         ('Flash Sale 5', '2026-09-19 00:00:00', '2026-09-19 23:59:59'),
                                                         ('Flash Sale 6', '2026-10-20 00:00:00', '2026-10-20 23:59:59'),
                                                         ('Flash Sale 7', '2026-11-11 00:00:00', '2026-11-11 23:59:59'),
                                                         ('Flash Sale 8', '2026-12-12 00:00:00', '2026-12-12 23:59:59'),
                                                         ('Flash Sale 9', '2026-04-23 00:00:00', '2026-04-23 23:59:59'),
                                                         ('Flash Sale 10', '2026-04-24 00:00:00', '2026-04-24 23:59:59'),
                                                         ('Flash Sale 11', '2026-04-25 00:00:00', '2026-04-25 23:59:59'),
                                                         ('Flash Sale 12', '2026-04-26 00:00:00', '2026-04-26 23:59:59'),
                                                         ('Flash Sale 13', '2026-04-27 00:00:00', '2026-04-27 23:59:59'),
                                                         ('Flash Sale 14', '2026-04-28 00:00:00', '2026-04-28 23:59:59'),
                                                         ('Sale Cuối Năm', '2026-12-31 18:00:00', '2026-12-31 23:59:59');

-- =======================================================================
-- 7. INSERT FLASH SALE ITEMS (Gán sản phẩm cho Sale ID 4 đang chạy)
-- =======================================================================
INSERT INTO flash_sale_items (flash_sale_id, variant_id, discount_percent, sale_stock) VALUES
                                                                                           (4, 1, 15, 5),
                                                                                           (4, 3, 20, 10),
                                                                                           (4, 12, 50, 20);

-- =======================================================================
-- 8. INSERT ORDERS (20 đơn hàng trong 2026)
-- =======================================================================
SET @customer_id = (SELECT id FROM users WHERE role = 'CUSTOMER' LIMIT 1);
SET @customer_id = COALESCE(@customer_id, 1);

-- =======================================================================
-- 8. INSERT ORDERS (Chỉ 2 dòng duy nhất)
-- =======================================================================
INSERT INTO orders (id, user_id, coupon_id, total_amount, status, shipping_address, created_at) VALUES
                                                                                                    (1, @customer_id, 1, 26100000.00, 'DELIVERED', '123 Nguyễn Trãi, Quận 1, HCM', '2026-04-10 10:00:00'),
                                                                                                    (2, @customer_id, NULL, 34000000.00, 'SHIPPING', '456 Lê Lợi, Hoàn Kiếm, Hà Nội', '2026-04-13 08:30:00');

-- =======================================================================
-- 9. INSERT ORDER DETAILS (Sửa lại khớp với 2 đơn hàng trên)
-- =======================================================================
-- Đơn hàng 1 có 1 sản phẩm iPhone 15 Pro Max (variant_id = 1)
-- Đơn hàng 2 có 1 sản phẩm biến thể khác (ví dụ variant_id = 2)
INSERT INTO order_details (order_id, variant_id, quantity, unit_price) VALUES
                                                                           (1, 1, 1, 29000000.00), -- ID 1 khớp với Orders ID 1
                                                                           (2, 2, 1, 34000000.00); -- ID 2 khớp với Orders ID 2

-- Hoàn tất cập nhật --