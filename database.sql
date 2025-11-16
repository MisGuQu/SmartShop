CREATE DATABASE smartshop_db;
USE smartshop_db;

-- ============================================================
-- 1. USERS
-- ============================================================
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE users_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- ============================================================
-- 2. PRODUCT & CATEGORY
-- ============================================================
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL UNIQUE,
    parent_id BIGINT,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    description TEXT,
    price DECIMAL(12,2) NOT NULL,
    has_variants BOOLEAN DEFAULT FALSE,
    category_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE product_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    public_id VARCHAR(255) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE product_variants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    variant_name VARCHAR(150) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    stock INT DEFAULT 0,
    sku VARCHAR(100) UNIQUE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE variant_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    variant_id BIGINT NOT NULL,
    public_id VARCHAR(255) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
);

-- ============================================================
-- 3. CART (gộp wishlist)
-- ============================================================
CREATE TABLE carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    variant_id BIGINT,
    quantity INT DEFAULT 1,
    is_wishlist BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE SET NULL
);

-- ============================================================
-- 4. ORDERS
-- ============================================================
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) UNIQUE,
    user_id BIGINT,
    status VARCHAR(30) DEFAULT 'PENDING',
    total_amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(20),
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    shipping_address TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT,
    variant_id BIGINT,
    quantity INT,
    price DECIMAL(12,2),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE order_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- ============================================================
-- 5. VOUCHER
-- ============================================================
CREATE TABLE vouchers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    type VARCHAR(20),
    value DECIMAL(12,2),
    min_order DECIMAL(12,2),
    start_date DATETIME,
    end_date DATETIME,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE user_vouchers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    voucher_id BIGINT,
    is_used BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE
);

-- ============================================================
-- 6. PAYMENT (VNPay / MoMo API)
-- ============================================================
CREATE TABLE payment_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    method VARCHAR(20),
    amount DECIMAL(12,2),
    status VARCHAR(20) DEFAULT 'PENDING',
    transaction_no VARCHAR(100),
    gateway_response TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- ============================================================
-- 7. REVIEW + MULTIPLE MEDIA
-- ============================================================
CREATE TABLE reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE review_media (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_id BIGINT NOT NULL,
    url VARCHAR(255) NOT NULL,
    type ENUM('IMAGE','VIDEO') DEFAULT 'IMAGE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
);

-- ============================================================
-- DỮ LIỆU MẪU
-- ============================================================

-- Roles
INSERT INTO roles(name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- Users
INSERT INTO users(username, password, email, full_name)
VALUES ('admin', '123456', 'admin@shop.com', 'Administrator'),
       ('nhi', '123456', 'nhi@example.com', 'Nhi Tran');

INSERT INTO users_roles VALUES (1,2), (2,1);

-- Categories
INSERT INTO categories(name) VALUES
('Thời trang'),
('Điện tử'),
('Phụ kiện');

-- Products
INSERT INTO products(name, slug, description, price, has_variants, category_id)
VALUES 
('Áo Thun Cotton', 'ao-thun-cotton', 'Áo thun cotton thoáng mát', 150000, TRUE, 1),
('Loa Bluetooth JBL', 'loa-jbl', 'JBL Flip 5 chống nước IPX7', 2300000, FALSE, 2),
('Chuột Gaming G102', 'chuot-g102', 'Chuột gaming Logitech G102', 350000, FALSE, 3);

-- Product images
INSERT INTO product_images(product_id, public_id, is_primary)
VALUES
(1, 'product/shirt/main', TRUE),
(2, 'product/jbl/main', TRUE),
(3, 'product/g102/main', TRUE);

-- Variants
INSERT INTO product_variants(product_id, variant_name, price, stock, sku)
VALUES
(1, 'Size M', 150000, 100, 'ATC-M'),
(1, 'Size L', 150000, 80, 'ATC-L');

-- Cart
INSERT INTO carts(user_id) VALUES (2);

INSERT INTO cart_items(cart_id, product_id, quantity)
VALUES (1, 3, 1);

-- Voucher
INSERT INTO vouchers(code, type, value, min_order, start_date, end_date)
VALUES ('WELCOME10', 'PERCENT', 10, 0, NOW(), '2026-12-31');

INSERT INTO user_vouchers(user_id, voucher_id) VALUES (2,1);

-- Order
INSERT INTO orders(order_number, user_id, total_amount, payment_method, shipping_address)
VALUES ('ORD-001', 2, 350000, 'COD', '123 Lê Lợi, TP HCM');

INSERT INTO order_items(order_id, product_id, quantity, price)
VALUES (1, 3, 1, 350000);

INSERT INTO payment_transactions(order_id, method, amount, status)
VALUES (1, 'COD', 350000, 'SUCCESS');

-- Review
INSERT INTO reviews(user_id, product_id, rating, comment)
VALUES (2, 3, 5, 'Chuột dùng rất tốt và mượt!');

-- Review Media
INSERT INTO review_media(review_id, url, type)
VALUES 
(1, 'review/g102_1.png', 'IMAGE'),
(1, 'review/g102_2.png', 'IMAGE'),
(1, 'review/g102_video.mp4', 'VIDEO');
