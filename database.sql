-- ============================================================
-- TẠO DATABASE
-- ============================================================
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
-- 2. CATEGORY
-- ============================================================
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL UNIQUE,
    parent_id BIGINT,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- ============================================================
-- 3. PRODUCTS
-- ============================================================
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    image_url VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    category_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- ============================================================
-- 4. CART + WISHLIST
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
    quantity INT DEFAULT 1,
    is_wishlist BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ============================================================
-- 5. ORDERS
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
    voucher_code VARCHAR(50),
    voucher_discount DECIMAL(12,2),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT,
    quantity INT,
    price DECIMAL(12,2),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
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
-- 6. VOUCHERS
-- ============================================================
CREATE TABLE vouchers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    type VARCHAR(20),
    value DECIMAL(12,2),
    min_order DECIMAL(12,2),
    start_date DATETIME,
    end_date DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    category_id BIGINT
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
-- 7. PAYMENT
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
-- 8. REVIEWS
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

INSERT INTO carts (id, user_id) VALUES
(2, 3),
(3, 4);

INSERT INTO cart_items (id, cart_id, product_id, quantity, is_wishlist) VALUES
(4, 3, 3, 1, 1);

INSERT INTO categories (id, name, parent_id) VALUES
(1, 'Thời trang', NULL),
(2, 'Điện tử', NULL),
(3, 'Phụ kiện', NULL);

INSERT INTO orders (id, order_number, user_id, status, total_amount, payment_method, payment_status, shipping_address, created_at, voucher_code, voucher_discount) VALUES
(1, 'ORD-001', NULL, 'PENDING', 350000, 'COD', 'PENDING', '123 Lê Lợi, TP HCM', '2025-11-20 17:06:37', NULL, NULL),
(2, 'ORD-B19FDF48', 4, 'SHIPPED', 1720000, 'COD', 'PENDING', 'Asa - 0789654123\nqwdqdqwdwdw', '2025-11-20 18:29:35', NULL, 0),
(3, 'ORD-BC6E2CAA', 4, 'PENDING', 160000, 'COD', 'PENDING', 'Asa - 0789654123\nfafdvrvwrggvsdsd', '2025-11-20 20:33:38', NULL, 0),
(4, 'ORD-D9BBA48E', 4, 'CANCELLED', 1840000, 'COD', 'PENDING', 'Asa - 0789654123\nsrsrgfdsgrg', '2025-11-20 20:48:22', 'FAST', 460000);

INSERT INTO order_items (id, order_id, product_id, quantity, price) VALUES
(1, 1, 3, 1, 350000),
(2, 2, 1, 2, 160000),
(3, 2, 3, 4, 350000),
(4, 3, 1, 1, 160000),
(5, 4, 2, 1, 2300000);

INSERT INTO order_status (id, order_id, old_status, new_status, created_at) VALUES
(1, 2, 'PENDING', 'PROCESSING', '2025-11-20 19:00:47'),
(2, 2, 'PROCESSING', 'SHIPPED', '2025-11-20 19:00:56'),
(3, 4, 'PENDING', 'CANCELLED', '2025-11-20 21:05:49');

INSERT INTO payment_transactions (id, order_id, method, amount, status, transaction_no, gateway_response, created_at) VALUES
(1, 1, 'COD', 350000, 'SUCCESS', NULL, NULL, '2025-11-20 17:06:37');

INSERT INTO products (id, name, description, price, stock_quantity, image_url, is_active, category_id, created_at, updated_at) VALUES
(1, 'Áo Thun Cotton', 'Áo thun cotton thoáng mát', 160000, 120, 'https://res.cloudinary.com/djmy7irgt/image/upload/v1763634948/smartshop/products/uyabcteeyoxegyovhxx3.jpg', 1, 1, '2025-11-20 17:06:37', '2025-11-20 20:33:38'),
(2, 'Loa Bluetooth JBL', 'JBL Flip 5 chống nước IPX7', 2300000, 59, 'https://res.cloudinary.com/djmy7irgt/image/upload/v1763635347/smartshop/products/etatcsoxzg4qdfurzvg1.jpg', 1, 2, '2025-11-20 17:06:37', '2025-11-20 20:48:22'),
(3, 'Chuột Gaming G102', 'Chuột gaming Logitech G102', 350000, 86, 'https://res.cloudinary.com/djmy7irgt/image/upload/v1763635154/smartshop/products/lgn0tifjau55akioobij.jpg', 1, 3, '2025-11-20 17:06:37', '2025-11-20 18:29:35'),
(5, 'fsfwef', 'gwrgwe', 40000, 2, 'https://res.cloudinary.com/djmy7irgt/image/upload/v1763642108/smartshop/products/ouyjwmpouyat4w3xffhp.jpg', 1, 1, '2025-11-20 19:35:07', '2025-11-20 19:35:10');

INSERT INTO roles (id, name) VALUES
(2, 'ROLE_ADMIN'),
(3, 'ROLE_CUSTOMER'),
(1, 'ROLE_USER');

INSERT INTO users (id, username, password, email, full_name, phone, avatar, is_active, created_at, updated_at) VALUES
(3, 'admin123@gmail.com', '$2a$10$/jN3.Vq0gqswBh3PnwHWFOYkaITArutJgS64dTeIN2r6EeN4Bh1WK', 'admin123@gmail.com', 'Admin', NULL, NULL, 1, '2025-11-20 17:10:18', '2025-11-20 17:10:18'),
(4, 'User123@gmail.com', '$2a$10$wNpI55Z0ANPJKPsKtJx44.m4HfR3zsqqsDn9Gt5XSkLTEJ/0zfQ5e', 'User123@gmail.com', 'User1', '56332962', 'https://res.cloudinary.com/djmy7irgt/image/upload/v1763644989/smartshop/users/p0qvqkqprxczu8phr1wu.jpg', 1, '2025-11-20 17:47:17', '2025-11-20 20:23:16');

INSERT INTO users_roles (user_id, role_id) VALUES
(3, 2),
(4, 3);

INSERT INTO user_vouchers (id, user_id, voucher_id, is_used) VALUES
(2, 4, 3, 0);

INSERT INTO vouchers (id, code, type, value, min_order, start_date, end_date, is_active, category_id) VALUES
(1, 'WELCOME10', 'PERCENTAGE', 10, 0, '2025-11-20 00:00:00', '2026-12-30 23:59:59', 1, NULL),
(3, 'FAST', 'PERCENTAGE', 20, 500000, '2025-11-19 00:00:00', '2025-11-27 23:59:59', 1, NULL);
