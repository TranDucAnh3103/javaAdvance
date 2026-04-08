/*
 * DATABASE SCHEMA: SMARTPHONE STORE MANAGEMENT (PRJ-PHONE-STORE-JAVA-02)
 */

/*
 * BẢNG 1: users
 * Mục đích: Quản lý định danh (Authentication) & Phân quyền (Authorization).
 * Kiến trúc: Gộp Admin & Customer dùng chung bảng. Phân biệt qua cột 'role'.
 * Bảo mật: Cột 'password' dài 255 char để chứa chuỗi mã hóa BCrypt.
 */
CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       phone VARCHAR(15) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       address VARCHAR(255),
                       role ENUM('ADMIN', 'CUSTOMER') DEFAULT 'CUSTOMER',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       is_active BOOLEAN DEFAULT TRUE
);

/*
 * BẢNG 2: categories
 * Mục đích: Phân nhóm sản phẩm chính (Điện thoại, Phụ kiện...).
 * Kiến trúc: Dùng 'is_deleted' để Xóa mềm (Soft Delete), đảm bảo toàn vẹn dữ liệu khóa ngoại.
 */
CREATE TABLE categories (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(100) UNIQUE NOT NULL,
                            is_deleted BOOLEAN DEFAULT FALSE
);

/*
 * BẢNG 3: products
 * Mục đích: Lưu thông tin cốt lõi, không biến động của sản phẩm.
 * Kiến trúc: Tách biệt thông tin chung (tên, hãng) khỏi các thông tin biến động (giá, kho).
 */
CREATE TABLE products (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          category_id INT NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          brand VARCHAR(100) NOT NULL,
                          description TEXT,
                          is_deleted BOOLEAN DEFAULT FALSE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (category_id) REFERENCES categories(id)
);

/*
 * BẢNG 4: product_variants
 * Mục đích: Quản lý các biến thể chi tiết (RAM, ROM, Màu) để bán và quản lý kho.
 * Kiến trúc: Ràng buộc CHECK ở tầng DB là chốt chặn cuối cùng chống lỗi tồn kho âm.
 */
CREATE TABLE product_variants (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  product_id INT NOT NULL,
                                  ram VARCHAR(50),
                                  rom VARCHAR(50),
                                  color VARCHAR(50),
                                  price DECIMAL(15, 2) NOT NULL CHECK (price > 0),
                                  stock INT NOT NULL CHECK (stock >= 0),
                                  is_deleted BOOLEAN DEFAULT FALSE,
                                  FOREIGN KEY (product_id) REFERENCES products(id)
);

/*
 * BẢNG 5: coupons (Mã giảm giá)
 * Mục đích: Quản lý chiến dịch khuyến mãi nhập mã thủ công.
 * Kiến trúc: Giới hạn nghiêm ngặt theo thời gian (valid_from/to) và số lượt sử dụng (usage_limit).
 */
CREATE TABLE coupons (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         code VARCHAR(50) UNIQUE NOT NULL,
                         discount_percent DECIMAL(5, 2) NOT NULL CHECK (discount_percent > 0 AND discount_percent <= 100),
                         valid_from TIMESTAMP NOT NULL,
                         valid_to TIMESTAMP NOT NULL,
                         usage_limit INT DEFAULT 1,
                         is_active BOOLEAN DEFAULT TRUE
);

/*
 * BẢNG 6: orders
 * Mục đích: Lưu trạng thái tổng quan của một giao dịch.
 * Kiến trúc: Lưu cứng 'shipping_address' và 'total_amount' (đã trừ Coupon) để truy vấn O(1) và giữ nguyên lịch sử giao dịch.
 */
CREATE TABLE orders (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL,
                        coupon_id INT NULL,
                        total_amount DECIMAL(15, 2) NOT NULL,
                        status ENUM('PENDING', 'SHIPPING', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
                        shipping_address VARCHAR(255) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id),
                        FOREIGN KEY (coupon_id) REFERENCES coupons(id)
);

/*
 * BẢNG 7: order_details
 * Mục đích: Map N-N giữa hóa đơn và biến thể sản phẩm.
 * Kiến trúc: Cột 'unit_price' ĐẢM BẢO tính bất biến của lịch sử tài chính, không bị ảnh hưởng nếu giá gốc đổi.
 */
CREATE TABLE order_details (
                               order_id INT NOT NULL,
                               variant_id INT NOT NULL,
                               quantity INT NOT NULL CHECK (quantity > 0),
                               unit_price DECIMAL(15, 2) NOT NULL,
                               PRIMARY KEY (order_id, variant_id),
                               FOREIGN KEY (order_id) REFERENCES orders(id),
                               FOREIGN KEY (variant_id) REFERENCES product_variants(id)
);

/*
 * BẢNG 8: flash_sales
 * Mục đích: Thiết lập bộ khung thời gian cho sự kiện giảm giá tự động.
 */
CREATE TABLE flash_sales (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             name VARCHAR(255) NOT NULL,
                             start_time TIMESTAMP NOT NULL,
                             end_time TIMESTAMP NOT NULL,
                             is_active BOOLEAN DEFAULT TRUE
);

/*
 * BẢNG 9: flash_sale_items
 * Mục đích: Cấu hình mặt hàng tham gia Flash Sale.
 * Kiến trúc: Cột 'sale_stock' tách biệt quỹ hàng Flash Sale khỏi kho gốc (stock). Chống bán vượt mức (Over-selling).
 */
CREATE TABLE flash_sale_items (
                                  flash_sale_id INT NOT NULL,
                                  variant_id INT NOT NULL,
                                  discount_percent DECIMAL(5, 2) NOT NULL CHECK (discount_percent > 0 AND discount_percent <= 100),
                                  sale_stock INT NOT NULL CHECK (sale_stock >= 0),
                                  PRIMARY KEY (flash_sale_id, variant_id),
                                  FOREIGN KEY (flash_sale_id) REFERENCES flash_sales(id),
                                  FOREIGN KEY (variant_id) REFERENCES product_variants(id)
);