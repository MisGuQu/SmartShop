-- ============================================================
-- RESET & CREATE DATABASE
-- ============================================================

CREATE DATABASE smartshop;
USE smartshop;

-- ============================================================
-- 1. USERS + ROLES
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
-- 2. CATEGORY (8 CATEGORY MỚI THEO MENU)
-- ============================================================
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL UNIQUE,
    parent_id BIGINT,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

INSERT INTO categories (id, name, parent_id) VALUES
(1,'Đồng Hồ Đeo Tay',NULL),
(2,'Máy Tính Xách Tay',NULL),
(3,'Máy Ảnh',NULL),
(4,'Điện Thoại',NULL),
(5,'Nước Hoa',NULL),
(6,'Nữ Trang',NULL),
(7,'Nón Thời Trang',NULL),
(8,'Túi Xách Du Lịch',NULL);

-- ============================================================
-- 3. PRODUCTS
-- ============================================================
CREATE TABLE products (
    id BIGINT PRIMARY KEY ,
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

-- ROLES
INSERT INTO roles (id, name) VALUES
(1,'ROLE_USER'),
(2,'ROLE_ADMIN'),
(3,'ROLE_CUSTOMER');

-- ============================
-- USERS (30 người – tên thực tế, email không dấu chấm)
-- ============================

-- Mỗi mật khẩu đã được mã hóa trực tiếp
-- Tất cả đều từ chuỗi gốc: 123456

INSERT INTO users (id, username, password, email, full_name, phone, avatar, is_active, created_at, updated_at) VALUES
(1,'admin','123456','admin123@gmail.com',NULL,'0900000000',NULL,1,'2025-01-05 10:00:00','2025-01-05 10:00:00'),

(2,'nguyenvana@gmail.com','123456','nguyenvana@gmail.com','Nguyễn Văn A','0900000001',NULL,1,'2025-01-10 09:00:00','2025-01-10 09:00:00'),

(3,'tranthib@gmail.com','123456','tranthib@gmail.com','Trần Thị B','0900000002',NULL,1,'2025-02-10 08:00:00','2025-02-10 08:00:00'),

(4,'lequangc@gmail.com','123456','lequangc@gmail.com','Lê Quang C','0900000003',NULL,1,'2025-03-12 14:00:00','2025-03-12 14:00:00'),

(5,'phamminhd@gmail.com','123456','phamminhd@gmail.com','Phạm Minh D','0900000004',NULL,1,'2025-04-05 11:00:00','2025-04-05 11:00:00'),

(6,'danghoae@gmail.com','123456','danghoae@gmail.com','Đặng Hoà E','0900000005',NULL,1,'2025-05-17 16:30:00','2025-05-17 16:30:00'),

(7,'hoanglongf@gmail.com','123456','hoanglongf@gmail.com','Hoàng Long F','0900000006',NULL,1,'2025-06-20 13:20:00','2025-06-20 13:20:00'),

(8,'buidieug@gmail.com','123456','buidieug@gmail.com','Bùi Diệu G','0900000007',NULL,1,'2025-07-22 09:45:00','2025-07-22 09:45:00'),

(9,'truonghanh@gmail.com','123456','truonghanh@gmail.com','Trương Hạnh H','0900000008',NULL,1,'2025-08-15 10:10:00','2025-08-15 10:10:00'),

(10,'doquangi@gmail.com','123456','doquangi@gmail.com','Đỗ Quang I','0900000009',NULL,1,'2025-09-09 19:30:00','2025-09-09 19:30:00'),

(11,'phamthanhj@gmail.com','123456','phamthanhj@gmail.com','Phạm Thanh J','0900000010',NULL,1,'2025-01-18 08:00:00','2025-01-18 08:00:00'),

(12,'huynhak@gmail.com','123456','huynhak@gmail.com','Huỳnh A K','0900000011',NULL,1,'2025-02-05 09:15:00','2025-02-05 09:15:00'),

(13,'trungkhoal@gmail.com','123456','trungkhoal@gmail.com','Trung Khoa L','0900000012',NULL,1,'2025-02-22 10:30:00','2025-02-22 10:30:00'),

(14,'lethanhm@gmail.com','123456','lethanhm@gmail.com','Lê Thành M','0900000013',NULL,1,'2025-03-08 11:45:00','2025-03-08 11:45:00'),

(15,'ngothanhn@gmail.com','123456','ngothanhn@gmail.com','Ngô Thanh N','0900000014',NULL,1,'2025-03-25 13:00:00','2025-03-25 13:00:00'),

(16,'vokhanho@gmail.com','123456','vokhanho@gmail.com','Võ Khánh O','0900000015',NULL,1,'2025-04-02 14:15:00','2025-04-02 14:15:00'),

(17,'truongminhp@gmail.com','123456','truongminhp@gmail.com','Trương Minh P','0900000016',NULL,1,'2025-04-18 15:30:00','2025-04-18 15:30:00'),

(18,'nguyenkhanhq@gmail.com','123456','nguyenkhanhq@gmail.com','Nguyễn Khánh Q','0900000017',NULL,1,'2025-05-03 16:45:00','2025-05-03 16:45:00'),

(19,'doantrungr@gmail.com','123456','doantrungr@gmail.com','Đoàn Trung R','0900000018',NULL,1,'2025-05-20 18:00:00','2025-05-20 18:00:00'),

(20,'duongminhs@gmail.com','123456','duongminhs@gmail.com','Dương Minh S','0900000019',NULL,1,'2025-06-06 19:15:00','2025-06-06 19:15:00'),

(21,'phamtrongt@gmail.com','123456','phamtrongt@gmail.com','Phạm Trọng T','0900000020',NULL,1,'2025-06-21 09:20:00','2025-06-21 09:20:00'),

(22,'hoanglinhu@gmail.com','123456','hoanglinhu@gmail.com','Hoàng Linh U','0900000021',NULL,1,'2025-07-02 10:25:00','2025-07-02 10:25:00'),

(23,'danghaiduyv@gmail.com','123456','danghaiduyv@gmail.com','Đặng Hải Duy V','0900000022',NULL,1,'2025-07-18 11:30:00','2025-07-18 11:30:00'),

(24,'ngocanhw@gmail.com','123456','ngocanhw@gmail.com','Ngọc Anh W','0900000023',NULL,1,'2025-08-01 13:40:00','2025-08-01 13:40:00'),

(25,'phamvanx@gmail.com','123456','phamvanx@gmail.com','Phạm Văn X','0900000024',NULL,1,'2025-08-16 15:00:00','2025-08-16 15:00:00'),

(26,'tranhoangy@gmail.com','123456','tranhoangy@gmail.com','Trần Hoàng Y','0900000025',NULL,1,'2025-09-01 16:10:00','2025-09-01 16:10:00'),

(27,'nguyenthivan@gmail.com','123456','nguyenthivan@gmail.com','Nguyễn Thị Vân','0900000026',NULL,1,'2025-09-15 17:20:00','2025-09-15 17:20:00'),

(28,'lephuongmai@gmail.com','123456','lephuongmai@gmail.com','Lê Phương Mai','0900000027',NULL,1,'2025-10-01 18:30:00','2025-10-01 18:30:00'),

(29,'buingoclam@gmail.com','123456','buingoclam@gmail.com','Bùi Ngọc Lâm','0900000028',NULL,1,'2025-10-15 19:40:00','2025-10-15 19:40:00'),

(30,'dangkimanh@gmail.com','123456','dangkimanh@gmail.com','Đặng Kim Anh','0900000029',NULL,1,'2025-11-01 20:50:00','2025-11-01 20:50:00');



-- USERS_ROLES
INSERT INTO users_roles (user_id, role_id) VALUES
(1,2),
(1,3),
(2,3),(3,3),(4,3),(5,3),(6,3),(7,3),(8,3),(9,3),
(10,3),(11,3),(12,3),(13,3),(14,3),(15,3),(16,3),
(17,3),(18,3),(19,3),(20,3),(21,3),(22,3),(23,3),
(24,3),(25,3),(26,3),(27,3),(28,3),(29,3),(30,3);

-- ============================================================
-- PRODUCTS (1–75, ĐÃ MAP CATEGORY HỢP LÝ, KHÔNG CÓ 76–100)
-- ============================================================

INSERT INTO products (id,name,description,price,stock_quantity,image_url,is_active,category_id,created_at,updated_at) VALUES
-- 1–25: NÓN THỜI TRANG (7)
(1,'Áo thun basic 01','Áo thun cotton cổ tròn',150000,120,'https://example.com/img/f1.jpg',1,7,'2025-01-05 10:00:00','2025-01-05 10:00:00'),
(2,'Áo thun basic 02','Áo thun tay ngắn',160000,110,'https://example.com/img/f2.jpg',1,7,'2025-01-08 11:00:00','2025-01-08 11:00:00'),
(3,'Áo sơ mi cổ điển','Sơ mi công sở',220000,90,'https://example.com/img/f3.jpg',1,7,'2025-01-12 09:30:00','2025-01-12 09:30:00'),
(4,'Quần jean nam 01','Quần jean ống đứng',320000,80,'https://example.com/img/f4.jpg',1,7,'2025-01-20 13:10:00','2025-01-20 13:10:00'),
(5,'Quần jean nữ 01','Quần jean nữ co giãn',330000,75,'https://example.com/img/f5.jpg',1,7,'2025-01-25 15:45:00','2025-01-25 15:45:00'),
(6,'Áo khoác dù mỏng','Áo khoác gió nhẹ',280000,60,'https://example.com/img/f6.jpg',1,7,'2025-02-02 08:20:00','2025-02-02 08:20:00'),
(7,'Áo khoác nỉ 01','Áo khoác nỉ có mũ',350000,50,'https://example.com/img/f7.jpg',1,7,'2025-02-08 10:00:00','2025-02-08 10:00:00'),
(8,'Quần short kaki nam','Quần short đi chơi',190000,100,'https://example.com/img/f8.jpg',1,7,'2025-02-15 09:40:00','2025-02-15 09:40:00'),
(9,'Quần tây công sở','Quần tây ống suông',300000,70,'https://example.com/img/f9.jpg',1,7,'2025-02-20 14:10:00','2025-02-20 14:10:00'),
(10,'Áo len mỏng','Áo len mặc thu đông',260000,65,'https://example.com/img/f10.jpg',1,7,'2025-03-01 16:00:00','2025-03-01 16:00:00'),
(11,'Váy chữ A 01','Váy chữ A đơn giản',270000,55,'https://example.com/img/f11.jpg',1,7,'2025-03-05 10:30:00','2025-03-05 10:30:00'),
(12,'Váy suông công sở','Váy suông dài gối',290000,45,'https://example.com/img/f12.jpg',1,7,'2025-03-10 09:00:00','2025-03-10 09:00:00'),
(13,'Áo polo nam 01','Áo polo cổ bẻ',210000,85,'https://example.com/img/f13.jpg',1,7,'2025-03-15 13:30:00','2025-03-15 13:30:00'),
(14,'Áo polo nữ 01','Áo polo form nữ',215000,75,'https://example.com/img/f14.jpg',1,7,'2025-03-18 17:00:00','2025-03-18 17:00:00'),
(15,'Áo cardigan mỏng','Cardigan len mỏng',240000,60,'https://example.com/img/f15.jpg',1,7,'2025-03-22 19:20:00','2025-03-22 19:20:00'),
(16,'Áo hoodie basic','Hoodie trơn unisex',320000,50,'https://example.com/img/f16.jpg',1,7,'2025-04-01 08:45:00','2025-04-01 08:45:00'),
(17,'Áo khoác bò','Áo khoác jean',420000,40,'https://example.com/img/f17.jpg',1,7,'2025-04-05 09:50:00','2025-04-05 09:50:00'),
(18,'Áo khoác gió giới hạn','Áo khoác phiên bản giới hạn',500000,5,'https://example.com/img/f18.jpg',1,7,'2025-04-10 10:10:00','2025-04-10 10:10:00'), -- stock < 10
(19,'Quần jogger unisex','Quần jogger bo gấu',260000,35,'https://example.com/img/f19.jpg',1,7,'2025-04-15 11:25:00','2025-04-15 11:25:00'),
(20,'Áo thun oversize 01','Áo thun rộng unisex',190000,95,'https://example.com/img/f20.jpg',1,7,'2025-04-20 12:00:00','2025-04-20 12:00:00'),
(21,'Áo thun oversize 02','Áo thun form rộng',195000,90,'https://example.com/img/f21.jpg',1,7,'2025-04-25 13:10:00','2025-04-25 13:10:00'),
(22,'Áo sơ mi kẻ caro','Sơ mi caro trẻ trung',230000,80,'https://example.com/img/f22.jpg',1,7,'2025-05-02 09:00:00','2025-05-02 09:00:00'),
(23,'Chân váy xếp ly','Váy xếp ly trung niên',280000,45,'https://example.com/img/f23.jpg',1,7,'2025-05-05 10:15:00','2025-05-05 10:15:00'),
(24,'Quần jean rách gối','Quần jean cá tính',340000,55,'https://example.com/img/f24.jpg',1,7,'2025-05-10 15:00:00','2025-05-10 15:00:00'),
(25,'Áo khoác dù có mũ','Áo khoác dù chống gió',360000,48,'https://example.com/img/f25.jpg',1,7,'2025-05-15 16:20:00','2025-05-15 16:20:00'),

-- 26–50: MÁY TÍNH XÁCH TAY (2) – thiết bị / phụ kiện
(26,'Tai nghe không dây 01','Tai nghe Bluetooth in-ear',450000,60,'https://example.com/img/e1.jpg',1,2,'2025-01-18 10:00:00','2025-01-18 10:00:00'),
(27,'Tai nghe chụp tai 01','Tai nghe chụp tai cách âm',650000,40,'https://example.com/img/e2.jpg',1,2,'2025-01-25 11:30:00','2025-01-25 11:30:00'),
(28,'Loa Bluetooth mini','Loa mini mang theo',380000,70,'https://example.com/img/e3.jpg',1,2,'2025-02-02 09:10:00','2025-02-02 09:10:00'),
(29,'Loa Bluetooth công suất lớn','Loa dùng cho phòng khách',1250000,35,'https://example.com/img/e4.jpg',1,2,'2025-02-10 15:25:00','2025-02-10 15:25:00'),
(30,'Bàn phím cơ 87 phím','Bàn phím cơ blue switch',890000,30,'https://example.com/img/e5.jpg',1,2,'2025-02-18 18:40:00','2025-02-18 18:40:00'),
(31,'Bàn phím văn phòng','Bàn phím màng mỏng',250000,80,'https://example.com/img/e6.jpg',1,2,'2025-02-24 08:20:00','2025-02-24 08:20:00'),
(32,'Chuột không dây văn phòng','Chuột êm, tiết kiệm pin',190000,90,'https://example.com/img/e7.jpg',1,2,'2025-03-03 10:10:00','2025-03-03 10:10:00'),
(33,'Chuột gaming RGB','Chuột game đổi màu',420000,50,'https://example.com/img/e8.jpg',1,2,'2025-03-08 12:00:00','2025-03-08 12:00:00'),
(34,'Ổ cứng SSD 250GB','SSD 2.5 inch',790000,40,'https://example.com/img/e9.jpg',1,2,'2025-03-15 13:35:00','2025-03-15 13:35:00'),
(35,'Ổ cứng SSD 500GB','SSD 500GB SATA',1150000,35,'https://example.com/img/e10.jpg',1,2,'2025-03-20 16:45:00','2025-03-20 16:45:00'),
(36,'USB 32GB 3.0','USB tốc độ cao 32GB',150000,120,'https://example.com/img/e11.jpg',1,2,'2025-03-25 09:30:00','2025-03-25 09:30:00'),
(37,'USB 64GB 3.0','USB 64GB',210000,100,'https://example.com/img/e12.jpg',1,2,'2025-04-01 11:15:00','2025-04-01 11:15:00'),
(38,'Bộ chia USB 4 cổng','Hub USB cho laptop',180000,75,'https://example.com/img/e13.jpg',1,2,'2025-04-06 10:05:00','2025-04-06 10:05:00'),
(39,'Chuột vertical ergonomics','Chuột dọc chống mỏi tay',350000,30,'https://example.com/img/e14.jpg',1,2,'2025-04-12 14:40:00','2025-04-12 14:40:00'),
(40,'Bộ loa 2.1 cho PC','Loa 2.1 nghe nhạc',950000,28,'https://example.com/img/e15.jpg',1,2,'2025-04-18 15:55:00','2025-04-18 15:55:00'),
(41,'Webcam Full HD 1080p','Webcam học online',520000,45,'https://example.com/img/e16.jpg',1,2,'2025-04-24 09:00:00','2025-04-24 09:00:00'),
(42,'Bộ phát WiFi mini','Router WiFi cho căn hộ nhỏ',430000,3,'https://example.com/img/e17.jpg',1,2,'2025-05-01 08:50:00','2025-05-01 08:50:00'), -- stock < 10
(43,'Adapter chuyển Type-C','Adapter Type-C sang USB-A',120000,60,'https://example.com/img/e18.jpg',1,2,'2025-05-05 10:10:00','2025-05-05 10:10:00'),
(44,'Cáp HDMI 2m','Cáp HDMI cho màn hình',90000,100,'https://example.com/img/e19.jpg',1,2,'2025-05-09 11:20:00','2025-05-09 11:20:00'),
(45,'Giá đỡ laptop gấp gọn','Stand laptop nhôm',260000,40,'https://example.com/img/e20.jpg',1,2,'2025-05-14 13:00:00','2025-05-14 13:00:00'),
(46,'Thảm lót chuột cỡ lớn','Thảm bàn phím + chuột',180000,70,'https://example.com/img/e21.jpg',1,2,'2025-05-18 16:35:00','2025-05-18 16:35:00'),
(47,'Bàn phím không dây 01','Bàn phím wireless',390000,55,'https://example.com/img/e22.jpg',1,2,'2025-05-22 09:55:00','2025-05-22 09:55:00'),
(48,'Chuột không dây silent','Chuột click êm',230000,65,'https://example.com/img/e23.jpg',1,2,'2025-05-26 15:05:00','2025-05-26 15:05:00'),
(49,'Loa soundbar mini','Soundbar cho màn hình',680000,25,'https://example.com/img/e24.jpg',1,2,'2025-05-30 18:10:00','2025-05-30 18:10:00'),
(50,'Đèn LED bàn phím','Đèn LED trang trí bàn phím',99000,120,'https://example.com/img/e25.jpg',1,2,'2025-06-02 20:00:00','2025-06-02 20:00:00'),

-- 51–75: PHỤ KIỆN, NỮ TRANG, TÚI XÁCH
(51,'Ba lô laptop 15.6 inch','Ba lô chống sốc',390000,50,'https://example.com/img/a1.jpg',1,8,'2025-02-01 10:00:00','2025-02-01 10:00:00'),
(52,'Túi đeo chéo nam','Túi vải canvas',260000,60,'https://example.com/img/a2.jpg',1,8,'2025-02-05 11:30:00','2025-02-05 11:30:00'),
(53,'Túi đeo chéo nữ','Túi nhỏ đi chơi',270000,55,'https://example.com/img/a3.jpg',1,8,'2025-02-10 09:45:00','2025-02-10 09:45:00'),
(54,'Ví da mini','Ví da bỏ túi',220000,70,'https://example.com/img/a4.jpg',1,6,'2025-02-15 13:20:00','2025-02-15 13:20:00'),
(55,'Thắt lưng da nam','Thắt lưng da bò',250000,80,'https://example.com/img/a5.jpg',1,6,'2025-02-20 14:15:00','2025-02-20 14:15:00'),
(56,'Nón lưỡi trai basic','Mũ lưỡi trai đơn giản',150000,90,'https://example.com/img/a6.jpg',1,7,'2025-02-25 15:40:00','2025-02-25 15:40:00'),
(57,'Nón bucket vải','Mũ bucket phong cách',170000,65,'https://example.com/img/a7.jpg',1,7,'2025-03-02 09:25:00','2025-03-02 09:25:00'),
(58,'Dây cáp sạc USB-C','Cáp sạc dài 1m',80000,120,'https://example.com/img/a8.jpg',1,2,'2025-03-07 08:30:00','2025-03-07 08:30:00'),
(59,'Dây cáp sạc Lightning','Cáp sạc thiết bị',120000,100,'https://example.com/img/a9.jpg',1,2,'2025-03-12 10:50:00','2025-03-12 10:50:00'),
(60,'Ổ cắm điện đa năng','Ổ cắm nhiều cổng',230000,75,'https://example.com/img/a10.jpg',1,2,'2025-03-17 12:10:00','2025-03-17 12:10:00'),
(61,'Dây rút velcro','Bộ dây rút quản lý dây',50000,200,'https://example.com/img/a11.jpg',1,2,'2025-03-22 14:00:00','2025-03-22 14:00:00'),
(62,'Hộp đựng phụ kiện','Hộp nhựa nhiều ngăn',90000,85,'https://example.com/img/a12.jpg',1,2,'2025-03-27 09:15:00','2025-03-27 09:15:00'),
(63,'Giá treo tai nghe','Giá treo dán cạnh bàn',110000,60,'https://example.com/img/a13.jpg',1,2,'2025-04-01 10:35:00','2025-04-01 10:35:00'),
(64,'Miếng lót bàn phím','Lót tay khi gõ',70000,95,'https://example.com/img/a14.jpg',1,2,'2025-04-05 11:40:00','2025-04-05 11:40:00'),
(65,'Bọc ghế văn phòng','Bọc ghế co giãn',180000,40,'https://example.com/img/a15.jpg',1,2,'2025-04-10 13:55:00','2025-04-10 13:55:00'),
(66,'Ốp bảo vệ chuột','Ốp silicon cho chuột',60000,70,'https://example.com/img/a16.jpg',1,2,'2025-04-15 16:10:00','2025-04-15 16:10:00'),
(67,'Bọc dây cáp chống rối','Bọc lò xo bảo vệ cáp',45000,150,'https://example.com/img/a17.jpg',1,2,'2025-04-20 09:05:00','2025-04-20 09:05:00'),
(68,'Túi chống sốc laptop','Túi đựng laptop đệm mút',210000,55,'https://example.com/img/a18.jpg',1,8,'2025-04-25 10:20:00','2025-04-25 10:20:00'),
(69,'Giá đỡ điện thoại bàn làm việc','Stand dựng thiết bị',90000,80,'https://example.com/img/a19.jpg',1,2,'2025-04-30 15:45:00','2025-04-30 15:45:00'),
(70,'Túi rút đựng đồ','Túi vải rút gọn',75000,65,'https://example.com/img/a20.jpg',1,8,'2025-05-03 09:20:00','2025-05-03 09:20:00'),
(71,'Dây đeo thẻ vải','Dây đeo thẻ nhân viên',30000,140,'https://example.com/img/a21.jpg',1,6,'2025-05-07 10:00:00','2025-05-07 10:00:00'),
(72,'Hộp đựng bút kim loại','Ống đựng bút bàn làm việc',65000,90,'https://example.com/img/a22.jpg',1,2,'2025-05-12 11:15:00','2025-05-12 11:15:00'),
(73,'Kẹp giấy nhiều màu','Kẹp giấy văn phòng',25000,200,'https://example.com/img/a23.jpg',1,2,'2025-05-18 08:00:00','2025-05-18 08:00:00'),
(74,'Giá treo balo sau cửa','Móc treo dán tường',85000,80,'https://example.com/img/a24.jpg',1,2,'2025-05-22 14:25:00','2025-05-22 14:25:00'),
(75,'Dây đeo thẻ da','Dây da đeo thẻ cao cấp',120000,8,'https://example.com/img/a25.jpg',1,6,'2025-05-28 09:30:00','2025-05-28 09:30:00');

-- ================================
--  ĐỒNG HỒ ĐEO TAY (1)
-- ================================
(76,'Đồng hồ nam dây thép 01','Đồng hồ chống nước, dây thép',1250000,20,'https://example.com/img/w1.jpg',1,1,'2025-06-10 10:00:00','2025-06-10 10:00:00'),
(77,'Đồng hồ nữ mặt tròn','Đồng hồ thời trang nữ',950000,30,'https://example.com/img/w2.jpg',1,1,'2025-06-12 11:00:00','2025-06-12 11:00:00'),
(78,'Đồng hồ nam dây da','Dây da cao cấp',1150000,25,'https://example.com/img/w3.jpg',1,1,'2025-06-14 12:00:00','2025-06-14 12:00:00'),
(79,'Đồng hồ đôi nam nữ','Đồng hồ cặp',1650000,15,'https://example.com/img/w4.jpg',1,1,'2025-06-16 13:00:00','2025-06-16 13:00:00'),
(80,'Đồng hồ thể thao điện tử','Đồng hồ điện tử đa chức năng',520000,40,'https://example.com/img/w5.jpg',1,1,'2025-06-18 14:00:00','2025-06-18 14:00:00'),
(81,'Đồng hồ nữ dây kim loại','Phong cách thanh lịch',980000,35,'https://example.com/img/w6.jpg',1,1,'2025-06-20 15:00:00','2025-06-20 15:00:00'),
(82,'Đồng hồ nam chống sốc','Mặt đồng hồ thể thao mạnh mẽ',750000,22,'https://example.com/img/w7.jpg',1,1,'2025-06-22 16:00:00','2025-06-22 16:00:00'),
(83,'Smartwatch basic 01','Đồng hồ thông minh đo bước chân',690000,28,'https://example.com/img/w8.jpg',1,1,'2025-06-25 17:00:00','2025-06-25 17:00:00'),
(84,'Smartwatch Pro','Đồng hồ thông minh đa tính năng',1550000,12,'https://example.com/img/w9.jpg',1,1,'2025-06-27 18:00:00','2025-06-27 18:00:00'),
(85,'Đồng hồ trẻ em chống nước','Đồng hồ cho bé trai/bé gái',320000,60,'https://example.com/img/w10.jpg',1,1,'2025-06-30 09:00:00','2025-06-30 09:00:00'),

-- ================================
--  MÁY ẢNH (3)
-- ================================
(86,'Máy ảnh DSLR Canon 1200D','Máy ảnh cho người mới bắt đầu',6500000,8,'https://example.com/img/c1.jpg',1,3,'2025-07-01 10:00:00','2025-07-01 10:00:00'),
(87,'Máy ảnh Sony A6000','Mirrorless Sony cảm biến APS-C',10500000,6,'https://example.com/img/c2.jpg',1,3,'2025-07-03 11:00:00','2025-07-03 11:00:00'),
(88,'Máy ảnh Fujifilm X-T100','Thiết kế cổ điển',8200000,10,'https://example.com/img/c3.jpg',1,3,'2025-07-05 12:00:00','2025-07-05 12:00:00'),
(89,'Máy quay cầm tay mini','Máy quay vlog nhỏ gọn',2500000,20,'https://example.com/img/c4.jpg',1,3,'2025-07-07 13:00:00','2025-07-07 13:00:00'),
(90,'Lens 50mm f/1.8 Canon','Ống kính chân dung',2500000,15,'https://example.com/img/c5.jpg',1,3,'2025-07-09 14:00:00','2025-07-09 14:00:00'),
(91,'Lens 35mm Sony','Ống kính góc rộng',5600000,12,'https://example.com/img/c6.jpg',1,3,'2025-07-10 15:00:00','2025-07-10 15:00:00'),
(92,'Tripod 1m8','Chân máy chụp ảnh',350000,50,'https://example.com/img/c7.jpg',1,3,'2025-07-11 16:00:00','2025-07-11 16:00:00'),
(93,'Gimbal chống rung','Thiết bị chống rung cho máy ảnh',2200000,18,'https://example.com/img/c8.jpg',1,3,'2025-07-12 17:00:00','2025-07-12 17:00:00'),
(94,'Đèn flash rời','Flash hỗ trợ chụp thiếu sáng',490000,35,'https://example.com/img/c9.jpg',1,3,'2025-07-13 18:00:00','2025-07-13 18:00:00'),
(95,'Túi đựng máy ảnh','Túi chống sốc chuyên dụng',320000,40,'https://example.com/img/c10.jpg',1,3,'2025-07-14 19:00:00','2025-07-14 19:00:00'),

-- ================================
--  ĐIỆN THOẠI (4)
-- ================================
(96,'Điện thoại Samsung A15','Điện thoại phổ thông',4500000,25,'https://example.com/img/p1.jpg',1,4,'2025-07-15 10:00:00','2025-07-15 10:00:00'),
(97,'iPhone 12 64GB','Điện thoại Apple',14500000,12,'https://example.com/img/p2.jpg',1,4,'2025-07-16 11:00:00','2025-07-16 11:00:00'),
(98,'Xiaomi Redmi Note 13','Pin trâu, cấu hình mạnh',5200000,30,'https://example.com/img/p3.jpg',1,4,'2025-07-17 12:00:00','2025-07-17 12:00:00'),
(99,'Oppo Reno 11','Camera siêu nét',10900000,14,'https://example.com/img/p4.jpg',1,4,'2025-07-18 13:00:00','2025-07-18 13:00:00'),
(100,'iPhone 13 128GB','Máy mới đẹp',17500000,10,'https://example.com/img/p5.jpg',1,4,'2025-07-19 14:00:00','2025-07-19 14:00:00'),
(101,'Samsung Galaxy S23','Flagship mạnh mẽ',19500000,6,'https://example.com/img/p6.jpg',1,4,'2025-07-20 15:00:00','2025-07-20 15:00:00'),
(102,'Tai nghe Bluetooth TWS','Tai nghe cho điện thoại',350000,80,'https://example.com/img/p7.jpg',1,4,'2025-07-21 16:00:00','2025-07-21 16:00:00'),
(103,'Ốp lưng iPhone 13','Ốp lưng trong suốt',150000,120,'https://example.com/img/p8.jpg',1,4,'2025-07-22 17:00:00','2025-07-22 17:00:00'),
(104,'Cáp sạc nhanh Type-C','Sạc nhanh 25W',180000,100,'https://example.com/img/p9.jpg',1,4,'2025-07-23 18:00:00','2025-07-23 18:00:00'),
(105,'Kính cường lực iPhone','Miếng dán cường lực',90000,150,'https://example.com/img/p10.jpg',1,4,'2025-07-24 19:00:00','2025-07-24 19:00:00'),

-- ================================
--  NƯỚC HOA (5)
-- ================================
(106,'Nước hoa Dior Sauvage','Hương nam tính mạnh mẽ',3250000,18,'https://example.com/img/pf1.jpg',1,5,'2025-07-25 10:00:00','2025-07-25 10:00:00'),
(107,'Nước hoa Chanel No.5','Hương cổ điển quyến rũ',4200000,10,'https://example.com/img/pf2.jpg',1,5,'2025-07-26 11:00:00','2025-07-26 11:00:00'),
(108,'Nước hoa Versace Bright Crystal','Hương ngọt nhẹ',2300000,20,'https://example.com/img/pf3.jpg',1,5,'2025-07-27 12:00:00','2025-07-27 12:00:00'),
(109,'Nước hoa Gucci Bloom','Hương hoa tinh tế',2950000,15,'https://example.com/img/pf4.jpg',1,5,'2025-07-28 13:00:00','2025-07-28 13:00:00'),
(110,'Nước hoa CK One','Hương unisex tươi mát',1800000,25,'https://example.com/img/pf5.jpg',1,5,'2025-07-29 14:00:00','2025-07-29 14:00:00');

-- ============================================================
-- VOUCHERS
-- ============================================================
INSERT INTO vouchers (id,code,type,value,min_order,start_date,end_date,is_active,category_id) VALUES
(1,'WELCOME10','PERCENTAGE',10,0,'2025-01-01 00:00:00','2025-12-31 23:59:59',1,NULL),
(2,'FAST20','PERCENTAGE',20,500000,'2025-03-01 00:00:00','2025-03-31 23:59:59',1,NULL),
(3,'SUMMER15','PERCENTAGE',15,700000,'2025-06-01 00:00:00','2025-06-30 23:59:59',1,NULL),
(4,'TECH5','PERCENTAGE',5,200000,'2025-09-01 00:00:00','2025-09-30 23:59:59',1,2),
(5,'FREESHIP','FIXED',30000,150000,'2025-10-01 00:00:00','2025-12-31 23:59:59',1,NULL);

INSERT INTO user_vouchers (id,user_id,voucher_id,is_used) VALUES
(1,2,1,0),
(2,3,2,1),
(3,4,3,0),
(4,5,4,0),
(5,6,5,0),
(6,7,1,0),
(7,8,3,0),
(8,9,2,1),
(9,10,5,0),
(10,11,1,0);

-- ============================================================
-- CARTS & CART_ITEMS
-- ============================================================
INSERT INTO carts (id,user_id) VALUES
(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10);

INSERT INTO cart_items (id,cart_id,product_id,quantity,is_wishlist) VALUES
(1,1,3,1,1),
(2,1,4,1,0),
(3,2,26,1,0),
(4,3,5,1,1),
(5,4,6,2,0),
(6,5,7,1,0),
(7,6,8,1,1),
(8,7,9,2,0),
(9,8,10,1,1),
(10,9,12,1,0);

-- ============================================================
-- ORDERS (50 ĐƠN)
-- ============================================================
INSERT INTO orders (id,order_number,user_id,status,total_amount,payment_method,payment_status,shipping_address,created_at,voucher_code,voucher_discount) VALUES
(1,'ORD-2025-001',2,'COMPLETED',320000,'COD','SUCCESS','HCM','2025-01-15 09:30:00',NULL,NULL),
(2,'ORD-2025-002',3,'PENDING',2300000,'COD','PENDING','HN','2025-02-10 10:10:00',NULL,NULL),
(3,'ORD-2025-003',4,'SHIPPED',510000,'COD','PENDING','Đà Nẵng','2025-03-20 14:00:00','WELCOME10',51000),
(4,'ORD-2025-004',5,'CANCELLED',1590000,'COD','FAILED','HCM','2025-04-11 16:45:00',NULL,NULL),
(5,'ORD-2025-005',6,'COMPLETED',440000,'COD','SUCCESS','HCM','2025-05-28 11:00:00','FAST20',88000),
(6,'ORD-2025-006',7,'PROCESSING',470000,'COD','PENDING','Huế','2025-06-08 13:00:00',NULL,NULL),
(7,'ORD-2025-007',8,'PENDING',280000,'COD','PENDING','HCM','2025-07-21 18:30:00',NULL,NULL),
(8,'ORD-2025-008',9,'COMPLETED',430000,'COD','SUCCESS','Cần Thơ','2025-08-12 09:55:00','SUMMER15',64500),
(9,'ORD-2025-009',10,'SHIPPED',350000,'COD','PENDING','Hà Nội','2025-09-18 19:40:00','TECH5',17500),
(10,'ORD-2025-010',2,'COMPLETED',500000,'COD','SUCCESS','HCM','2025-10-29 08:20:00','FREESHIP',30000),
(11,'ORD-2025-011',3,'COMPLETED',160000,'COD','SUCCESS','HCM','2025-11-03 15:15:00',NULL,NULL),
(12,'ORD-2025-012',4,'CANCELLED',350000,'COD','FAILED','HCM','2025-12-01 10:05:00',NULL,NULL),
(13,'ORD-2025-013',5,'COMPLETED',180000,'COD','SUCCESS','HCM','2025-01-25 09:00:00',NULL,NULL),
(14,'ORD-2025-014',6,'COMPLETED',210000,'COD','SUCCESS','HCM','2025-02-27 10:30:00',NULL,NULL),
(15,'ORD-2025-015',7,'PENDING',1290000,'COD','PENDING','HCM','2025-03-15 11:45:00',NULL,NULL),
(16,'ORD-2025-016',8,'COMPLETED',480000,'COD','SUCCESS','HCM','2025-06-20 08:20:00',NULL,NULL),
(17,'ORD-2025-017',9,'COMPLETED',700000,'COD','SUCCESS','HCM','2025-07-18 12:10:00',NULL,NULL),
(18,'ORD-2025-018',10,'SHIPPED',530000,'COD','PENDING','HCM','2025-08-25 19:15:00',NULL,NULL),
(19,'ORD-2025-019',3,'PENDING',300000,'COD','PENDING','HCM','2025-10-05 17:00:00',NULL,NULL),
(20,'ORD-2025-020',4,'COMPLETED',640000,'COD','SUCCESS','HCM','2025-12-20 20:30:00',NULL,NULL),
(21,'ORD-2025-021',11,'COMPLETED',450000,'COD','SUCCESS','HCM','2025-01-18 09:40:00',NULL,NULL),
(22,'ORD-2025-022',12,'PENDING',260000,'COD','PENDING','HN','2025-02-06 10:20:00',NULL,0),
(23,'ORD-2025-023',13,'COMPLETED',530000,'COD','SUCCESS','Đà Nẵng','2025-02-25 14:50:00','WELCOME10',53000),
(24,'ORD-2025-024',14,'CANCELLED',390000,'COD','FAILED','HCM','2025-03-05 16:25:00',NULL,NULL),
(25,'ORD-2025-025',15,'COMPLETED',780000,'COD','SUCCESS','HCM','2025-03-22 19:10:00',NULL,NULL),
(26,'ORD-2025-026',16,'PROCESSING',220000,'COD','PENDING','Huế','2025-04-03 11:15:00',NULL,NULL),
(27,'ORD-2025-027',17,'COMPLETED',360000,'COD','SUCCESS','HCM','2025-04-19 13:30:00',NULL,NULL),
(28,'ORD-2025-028',18,'PENDING',410000,'COD','PENDING','HCM','2025-05-07 09:55:00',NULL,NULL),
(29,'ORD-2025-029',19,'COMPLETED',990000,'COD','SUCCESS','HCM','2025-05-23 18:45:00','FAST20',198000),
(30,'ORD-2025-030',20,'SHIPPED',260000,'COD','PENDING','Cần Thơ','2025-06-02 08:30:00',NULL,NULL),
(31,'ORD-2025-031',21,'COMPLETED',380000,'COD','SUCCESS','HCM','2025-06-18 10:15:00',NULL,NULL),
(32,'ORD-2025-032',22,'PENDING',290000,'COD','PENDING','HCM','2025-07-04 11:20:00',NULL,NULL),
(33,'ORD-2025-033',23,'COMPLETED',520000,'COD','SUCCESS','HCM','2025-07-20 14:00:00','SUMMER15',78000),
(34,'ORD-2025-034',24,'COMPLETED',610000,'COD','SUCCESS','HCM','2025-08-03 15:35:00',NULL,NULL),
(35,'ORD-2025-035',25,'PENDING',450000,'COD','PENDING','HCM','2025-08-18 16:40:00',NULL,NULL),
(36,'ORD-2025-036',26,'COMPLETED',900000,'COD','SUCCESS','HCM','2025-09-02 17:50:00','TECH5',45000),
(37,'ORD-2025-037',27,'CANCELLED',310000,'COD','FAILED','HCM','2025-09-16 18:30:00',NULL,NULL),
(38,'ORD-2025-038',28,'COMPLETED',270000,'COD','SUCCESS','HN','2025-10-01 09:10:00',NULL,NULL),
(39,'ORD-2025-039',29,'SHIPPED',640000,'COD','PENDING','Đà Nẵng','2025-10-12 13:45:00',NULL,NULL),
(40,'ORD-2025-040',30,'COMPLETED',720000,'COD','SUCCESS','HCM','2025-10-28 19:20:00','FREESHIP',30000),
(41,'ORD-2025-041',11,'COMPLETED',350000,'COD','SUCCESS','HCM','2025-11-05 10:25:00',NULL,NULL),
(42,'ORD-2025-042',12,'PENDING',190000,'COD','PENDING','HN','2025-11-12 11:30:00',NULL,NULL),
(43,'ORD-2025-043',13,'COMPLETED',260000,'COD','SUCCESS','Đà Nẵng','2025-11-20 14:40:00',NULL,NULL),
(44,'ORD-2025-044',14,'COMPLETED',480000,'COD','SUCCESS','Cần Thơ','2025-11-28 15:50:00','WELCOME10',48000),
(45,'ORD-2025-045',15,'PENDING',320000,'COD','PENDING','HCM','2025-12-03 09:05:00',NULL,NULL),
(46,'ORD-2025-046',16,'COMPLETED',410000,'COD','SUCCESS','HCM','2025-12-08 13:15:00',NULL,NULL),
(47,'ORD-2025-047',17,'SHIPPED',560000,'COD','PENDING','HCM','2025-12-14 16:25:00',NULL,NULL),
(48,'ORD-2025-048',18,'COMPLETED',295000,'COD','SUCCESS','HN','2025-12-20 18:40:00',NULL,NULL),
(49,'ORD-2025-049',19,'COMPLETED',650000,'COD','SUCCESS','Đà Nẵng','2025-12-25 19:50:00','SUMMER15',97500),
(50,'ORD-2025-050',20,'CANCELLED',230000,'COD','FAILED','HCM','2025-12-30 20:30:00',NULL,NULL);

-- ORDER_ITEMS (không tham chiếu sản phẩm > 75)
INSERT INTO order_items (id,order_id,product_id,quantity,price) VALUES
(1,1,1,2,160000),
(2,2,26,1,2300000),
(3,3,3,1,350000),
(4,3,4,1,250000),
(5,4,30,1,1590000),
(6,5,1,1,160000),
(7,5,6,1,280000),
(8,6,7,1,320000),
(9,6,9,1,150000),
(10,7,8,1,280000),
(11,8,6,1,120000),
(12,8,10,1,350000),
(13,9,3,1,350000),
(14,10,4,1,250000),
(15,10,9,1,150000),
(16,11,1,1,160000),
(17,12,10,1,350000),
(18,13,13,1,180000),
(19,14,14,1,210000),
(20,15,15,1,1290000),
(21,16,7,1,320000),
(22,16,6,1,160000),
(23,17,11,1,220000),
(24,17,12,1,300000),
(25,18,26,1,2300000),
(26,19,9,2,150000),
(27,20,30,1,1590000),
(28,21,2,1,160000),
(29,21,52,1,290000),
(30,22,8,1,190000),
(31,23,5,1,330000),
(32,23,36,1,200000),
(33,24,51,1,390000),
(34,25,29,1,780000),
(35,26,11,1,210000),
(36,27,18,1,500000),
(37,28,9,1,300000),
(38,29,29,1,1250000),
(39,29,43,1,120000),
(40,30,10,1,260000),
(41,31,10,1,260000),
(42,32,20,1,190000),
(43,33,41,1,520000),
(44,34,52,1,430000),
(45,35,4,1,320000),
(46,36,34,1,790000),
(47,37,54,1,220000),
(48,38,12,1,290000),
(49,39,40,1,950000),
(50,40,41,1,520000),
(51,41,3,1,220000),
(52,42,1,1,150000),
(53,43,9,1,300000),
(54,44,28,1,380000),
(55,45,22,1,230000),
(56,46,17,1,420000),
(57,47,27,1,650000),
(58,48,61,1,50000),
(59,49,29,1,1250000),
(60,50,55,1,250000);

-- ORDER_STATUS
INSERT INTO order_status (id,order_id,old_status,new_status,created_at) VALUES
(1,1,'PENDING','COMPLETED','2025-01-16 10:00:00'),
(2,3,'PENDING','SHIPPED','2025-03-21 09:00:00'),
(3,4,'PENDING','CANCELLED','2025-04-12 08:00:00'),
(4,5,'PENDING','COMPLETED','2025-05-29 12:00:00'),
(5,8,'PENDING','COMPLETED','2025-08-13 14:30:00'),
(6,10,'PENDING','COMPLETED','2025-10-30 10:00:00'),
(7,11,'PENDING','COMPLETED','2025-11-04 09:00:00'),
(8,12,'PENDING','CANCELLED','2025-12-02 09:30:00'),
(9,16,'PENDING','COMPLETED','2025-06-21 09:40:00'),
(10,17,'PENDING','COMPLETED','2025-07-19 11:00:00'),
(11,18,'PENDING','SHIPPED','2025-08-26 10:10:00'),
(12,20,'PENDING','COMPLETED','2025-12-21 08:50:00'),
(13,21,'PENDING','COMPLETED','2025-01-18 10:30:00'),
(14,23,'PENDING','COMPLETED','2025-02-26 10:30:00'),
(15,25,'PENDING','COMPLETED','2025-03-23 10:00:00'),
(16,27,'PENDING','COMPLETED','2025-04-20 09:30:00'),
(17,29,'PENDING','COMPLETED','2025-05-24 10:15:00'),
(18,31,'PENDING','COMPLETED','2025-06-19 11:00:00'),
(19,33,'PENDING','COMPLETED','2025-07-21 15:00:00'),
(20,34,'PENDING','COMPLETED','2025-08-04 16:00:00'),
(21,36,'PENDING','COMPLETED','2025-09-03 18:00:00'),
(22,38,'PENDING','COMPLETED','2025-10-02 10:00:00'),
(23,40,'PENDING','COMPLETED','2025-10-29 20:00:00'),
(24,41,'PENDING','COMPLETED','2025-11-06 11:00:00'),
(25,43,'PENDING','COMPLETED','2025-11-21 15:00:00'),
(26,44,'PENDING','COMPLETED','2025-11-29 16:00:00'),
(27,46,'PENDING','COMPLETED','2025-12-09 14:00:00'),
(28,48,'PENDING','COMPLETED','2025-12-21 19:30:00'),
(29,49,'PENDING','COMPLETED','2025-12-26 20:30:00'),
(30,37,'PENDING','CANCELLED','2025-09-17 09:00:00'),
(31,50,'PENDING','CANCELLED','2025-12-31 09:00:00');

-- PAYMENT_TRANSACTIONS (cho các đơn COMPLETED)
INSERT INTO payment_transactions (id,order_id,method,amount,status,transaction_no,gateway_response,created_at) VALUES
(1,1,'COD',320000,'SUCCESS',NULL,NULL,'2025-01-15 09:40:00'),
(2,5,'COD',440000,'SUCCESS',NULL,NULL,'2025-05-28 11:10:00'),
(3,8,'COD',430000,'SUCCESS',NULL,NULL,'2025-08-12 10:05:00'),
(4,10,'COD',500000,'SUCCESS',NULL,NULL,'2025-10-29 08:30:00'),
(5,11,'COD',160000,'SUCCESS',NULL,NULL,'2025-11-03 15:20:00'),
(6,13,'COD',180000,'SUCCESS',NULL,NULL,'2025-01-25 09:15:00'),
(7,14,'COD',210000,'SUCCESS',NULL,NULL,'2025-02-27 10:40:00'),
(8,16,'COD',480000,'SUCCESS',NULL,NULL,'2025-06-20 08:30:00'),
(9,17,'COD',700000,'SUCCESS',NULL,NULL,'2025-07-18 12:20:00'),
(10,20,'COD',640000,'SUCCESS',NULL,NULL,'2025-12-20 20:40:00'),
(11,21,'COD',450000,'SUCCESS',NULL,NULL,'2025-01-18 10:00:00'),
(12,23,'COD',530000,'SUCCESS',NULL,NULL,'2025-02-25 15:10:00'),
(13,25,'COD',780000,'SUCCESS',NULL,NULL,'2025-03-22 19:30:00'),
(14,27,'COD',360000,'SUCCESS',NULL,NULL,'2025-04-19 13:50:00'),
(15,29,'COD',990000,'SUCCESS',NULL,NULL,'2025-05-23 19:00:00'),
(16,31,'COD',380000,'SUCCESS',NULL,NULL,'2025-06-18 10:30:00'),
(17,33,'COD',520000,'SUCCESS',NULL,NULL,'2025-07-20 14:20:00'),
(18,34,'COD',610000,'SUCCESS',NULL,NULL,'2025-08-03 16:00:00'),
(19,36,'COD',900000,'SUCCESS',NULL,NULL,'2025-09-02 18:10:00'),
(20,38,'COD',270000,'SUCCESS',NULL,NULL,'2025-10-01 09:30:00'),
(21,40,'COD',720000,'SUCCESS',NULL,NULL,'2025-10-28 19:40:00'),
(22,41,'COD',350000,'SUCCESS',NULL,NULL,'2025-11-05 10:40:00'),
(23,43,'COD',260000,'SUCCESS',NULL,NULL,'2025-11-20 15:00:00'),
(24,44,'COD',480000,'SUCCESS',NULL,NULL,'2025-11-28 16:10:00'),
(25,46,'COD',410000,'SUCCESS',NULL,NULL,'2025-12-08 13:30:00'),
(26,48,'COD',295000,'SUCCESS',NULL,NULL,'2025-12-20 19:00:00'),
(27,49,'COD',650000,'SUCCESS',NULL,NULL,'2025-12-25 20:10:00');

-- ============================================================
-- REVIEWS (chỉ dùng product_id <= 75)
-- ============================================================
INSERT INTO reviews (id,user_id,product_id,rating,comment,created_at) VALUES
(1,2,1,5,'Áo đẹp, chất vải tốt','2025-01-20 11:00:00'),
(2,3,26,4,'Loa nghe ổn trong tầm giá','2025-02-25 18:30:00'),
(3,4,3,5,'Chuột dùng rất mượt','2025-03-25 09:45:00'),
(4,5,30,4,'Bàn phím gõ sướng, hơi ồn','2025-04-15 20:00:00'),
(5,6,7,5,'Hoodie đẹp, ấm','2025-07-01 08:10:00'),
(6,7,10,4,'Áo len mặc khá ấm','2025-10-31 13:00:00'),
(7,8,6,5,'Áo khoác mỏng, dễ mặc','2025-06-12 09:20:00'),
(8,9,50,4,'Đèn LED đủ sáng, dễ dùng','2025-06-25 21:10:00'),
(9,10,13,5,'Áo polo mặc rất thoải mái','2025-07-10 10:05:00'),
(10,3,14,4,'Polo nữ lên form đẹp','2025-08-02 16:40:00'),
(11,4,27,5,'Tai nghe dùng lâu không đau tai','2025-08-20 19:00:00'),
(12,5,4,4,'Quần jean mặc thoải mái','2025-02-28 14:15:00'),
(13,6,36,5,'USB chép nhanh, ổn định','2025-05-10 11:30:00'),
(14,7,8,4,'Quần short dễ phối đồ','2025-04-30 17:50:00'),
(15,8,12,5,'Váy đẹp, đúng mô tả','2025-06-22 09:55:00'),
(16,9,29,5,'Loa công suất lớn, nghe đã','2025-07-15 11:00:00'),
(17,10,11,4,'Form váy đẹp, chất vải ổn','2025-08-05 12:00:00'),
(18,11,2,5,'Áo thun đẹp, vải mịn','2025-03-18 10:30:00'),
(19,12,32,4,'Chuột không dây cầm vừa tay','2025-05-22 13:00:00'),
(20,13,34,5,'SSD nhanh, máy mượt hơn hẳn','2025-06-10 09:45:00'),
(21,14,51,4,'Ba lô đựng laptop chắc chắn','2025-07-02 15:20:00'),
(22,15,52,4,'Túi đeo chéo đẹp, đường may ổn','2025-08-08 16:40:00'),
(23,6,53,5,'Túi nữ nhỏ gọn, tiện mang đi chơi','2025-09-01 18:10:00'),
(24,7,54,4,'Ví da mềm, đựng vừa tiền','2025-09-10 11:30:00'),
(25,8,60,4,'Ổ cắm điện tiện lợi, nhiều lỗ','2025-10-02 09:40:00'),
(26,9,68,5,'Túi chống sốc ôm laptop tốt','2025-10-18 14:20:00'),
(27,10,50,5,'Đèn bàn sáng rõ, chống chói','2025-11-05 20:00:00'),
(28,11,52,4,'Túi đeo chắc chắn, dây êm','2025-11-20 10:15:00'),
(29,12,54,4,'Ví da nhỏ gọn, đẹp','2025-12-02 13:50:00'),
(30,13,68,5,'Túi chống sốc dày, bảo vệ tốt','2025-12-15 09:25:00');

-- REVIEW_MEDIA: để trống, có thể thêm khi cần
