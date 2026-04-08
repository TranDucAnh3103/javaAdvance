DELIMITER $$

-- 1. LẤY DANH SÁCH
DROP PROCEDURE IF EXISTS sp_GetAllProducts$$
CREATE PROCEDURE sp_GetAllProducts(IN p_limit INT, IN p_offset INT)
BEGIN
SELECT p.id, p.name, c.name AS category_name, p.brand,
       MIN(v.price) AS min_price, MAX(v.price) AS max_price, SUM(v.stock) AS total_stock
FROM products p
         JOIN categories c ON p.category_id = c.id
         LEFT JOIN product_variants v ON p.id = v.product_id AND v.is_deleted = FALSE
WHERE p.is_deleted = FALSE
GROUP BY p.id, p.name, c.name, p.brand
ORDER BY p.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END$$

-- 2. TÌM KIẾM SẢN PHẨM (Phân trang + Keyword)
DROP PROCEDURE IF EXISTS sp_SearchProducts$$
CREATE PROCEDURE sp_SearchProducts(IN p_keyword VARCHAR(255), IN p_limit INT, IN p_offset INT)
BEGIN
SELECT p.id, p.name, c.name AS category_name, p.brand,
       MIN(v.price) AS min_price, MAX(v.price) AS max_price, SUM(v.stock) AS total_stock
FROM products p
         JOIN categories c ON p.category_id = c.id
         LEFT JOIN product_variants v ON p.id = v.product_id AND v.is_deleted = FALSE
WHERE p.is_deleted = FALSE AND p.name LIKE CONCAT('%', p_keyword, '%')
GROUP BY p.id, p.name, c.name, p.brand
ORDER BY p.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END$$

-- 3. CHECKOUT (Giao Transaction & Error cho Java)
DROP PROCEDURE IF EXISTS sp_Checkout_SingleItem$$
CREATE PROCEDURE sp_Checkout_SingleItem(
    IN p_user_id INT,
    IN p_variant_id INT,
    IN p_quantity INT,
    IN p_coupon_code VARCHAR(50),
    IN p_shipping_address VARCHAR(255),
    OUT p_order_id INT
)
BEGIN
    DECLARE v_current_stock INT;
    DECLARE v_unit_price DECIMAL(15,2);
    DECLARE v_coupon_id INT DEFAULT NULL;
    DECLARE v_discount_percent DECIMAL(5,2) DEFAULT 0;
    DECLARE v_total_amount DECIMAL(15,2);

    -- Khóa dòng (Row-level lock) chờ Java Commit/Rollback
SELECT stock, price INTO v_current_stock, v_unit_price
FROM product_variants WHERE id = p_variant_id AND is_deleted = FALSE FOR UPDATE;

-- Ném Exception về Java nếu lỗi Logic
IF v_current_stock IS NULL THEN
        SIGNAL SQLSTATE '45001' SET MESSAGE_TEXT = 'PRODUCT_NOT_FOUND';
    ELSEIF v_current_stock < p_quantity THEN
        SIGNAL SQLSTATE '45002' SET MESSAGE_TEXT = 'INSUFFICIENT_STOCK';
END IF;

    IF p_coupon_code IS NOT NULL AND p_coupon_code != '' THEN
SELECT id, discount_percent INTO v_coupon_id, v_discount_percent
FROM coupons
WHERE code = p_coupon_code AND is_active = TRUE AND usage_limit > 0
  AND CURRENT_TIMESTAMP BETWEEN valid_from AND valid_to FOR UPDATE;

IF v_coupon_id IS NULL THEN
            SIGNAL SQLSTATE '45003' SET MESSAGE_TEXT = 'INVALID_COUPON';
END IF;

UPDATE coupons SET usage_limit = usage_limit - 1 WHERE id = v_coupon_id;
END IF;

    SET v_total_amount = (v_unit_price * p_quantity) * (1 - v_discount_percent / 100);

UPDATE product_variants SET stock = stock - p_quantity WHERE id = p_variant_id;

INSERT INTO orders (user_id, coupon_id, total_amount, status, shipping_address)
VALUES (p_user_id, v_coupon_id, v_total_amount, 'PENDING', p_shipping_address);

SET p_order_id = LAST_INSERT_ID();

INSERT INTO order_details (order_id, variant_id, quantity, unit_price)
VALUES (p_order_id, p_variant_id, p_quantity, v_unit_price);
END$$

DELIMITER ;