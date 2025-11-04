CREATE DATABASE smartshop_db;
USE smartshop_db;

smartshop_db
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE COMMENT 'Tên đăng nhập',
    password VARCHAR(255) NOT NULL COMMENT 'Mật khẩu mã hóa BCrypt',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT 'Email',
    full_name VARCHAR(255) NOT NULL COMMENT 'Họ tên đầy đủ',
    phone VARCHAR(20) COMMENT 'Số điện thoại',
    avatar_url VARCHAR(500) COMMENT 'Ảnh đại diện từ Cloud Storage',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Trạng thái tài khoản',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) COMMENT 'Thông tin người dùng';

CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT 'Tên vai trò: ROLE_USER, ROLE_ADMIN'
) COMMENT 'Vai trò người dùng';

CREATE TABLE users_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) COMMENT 'Liên kết user với role';

-- ═══════════════════════════════════════════════════════════════
-- 2. QUẢN LÝ SẢN PHẨM
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Tên danh mục',
    description TEXT COMMENT 'Mô tả danh mục',
    image_url VARCHAR(500) COMMENT 'Ảnh danh mục',
    parent_id BIGINT COMMENT 'Danh mục cha (nếu có cấp bậc)',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) COMMENT 'Danh mục sản phẩm';

CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL COMMENT 'Tên sản phẩm',
    slug VARCHAR(255) UNIQUE COMMENT 'URL-friendly name: ao-thun-basic',
    description TEXT COMMENT 'Mô tả chi tiết',
    base_price DECIMAL(10, 2) NOT NULL COMMENT 'Giá cơ bản',
    
    -- QUAN TRỌNG: Xác định có variants không
    has_variants BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'TRUE = có size/màu/inch, FALSE = mua ngay',
    
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Trạng thái sản phẩm',
    category_id BIGINT,
    brand VARCHAR(100) COMMENT 'Thương hiệu',
    weight DECIMAL(8, 2) COMMENT 'Trọng lượng (kg)',
    
    -- SEO
    meta_title VARCHAR(255) COMMENT 'Tiêu đề SEO',
    meta_description TEXT COMMENT 'Mô tả SEO',
    
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) COMMENT 'Sản phẩm chính';

-- Bảng lưu ảnh sản phẩm (nhiều ảnh cho 1 sản phẩm)
CREATE TABLE product_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL COMMENT 'URL ảnh từ Cloud Storage',
    is_primary BOOLEAN DEFAULT FALSE COMMENT 'Ảnh chính/đại diện',
    display_order INT DEFAULT 0 COMMENT 'Thứ tự hiển thị',
    alt_text VARCHAR(255) COMMENT 'Mô tả ảnh (SEO)',
    created_at DATETIME NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) COMMENT 'Ảnh sản phẩm - upload nhiều ảnh';

CREATE TABLE product_variants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    
    -- CÁC THUỘC TÍNH (để NULL nếu không dùng)
    size VARCHAR(50) COMMENT 'Size: S, M, L, XL',
    color VARCHAR(50) COMMENT 'Màu sắc: Đỏ, Xanh, Đen',
    color_code VARCHAR(20) COMMENT 'Mã màu HEX: #FF0000',
    screen_size VARCHAR(50) COMMENT 'Kích thước màn hình: 43", 55", 65"',
    storage VARCHAR(50) COMMENT 'Bộ nhớ: 64GB, 128GB, 256GB',
    material VARCHAR(50) COMMENT 'Chất liệu',
    
    -- Giá và kho
    price DECIMAL(10, 2) NOT NULL COMMENT 'Giá bán',
    compare_at_price DECIMAL(10, 2) COMMENT 'Giá gốc (để hiển thị giảm giá)',
    stock_quantity INT NOT NULL DEFAULT 0 COMMENT 'Tồn kho',
    
    sku VARCHAR(100) UNIQUE COMMENT 'Mã SKU: SHIRT-RED-M',
    barcode VARCHAR(100) COMMENT 'Mã vạch',
    
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) COMMENT 'Phân loại sản phẩm (variants)';

-- Ảnh riêng cho từng variant
CREATE TABLE variant_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    variant_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL COMMENT 'URL ảnh variant',
    is_primary BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
) COMMENT 'Ảnh riêng cho từng variant (VD: ảnh áo màu đỏ)';

-- ═══════════════════════════════════════════════════════════════
-- 3. GIỎ HÀNG VÀ WISHLIST
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE shopping_carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT 'Giỏ hàng của user';

CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT COMMENT 'Sản phẩm gốc (nếu không có variants)',
    variant_id BIGINT COMMENT 'Variant đã chọn (nếu có variants)',
    quantity INT NOT NULL DEFAULT 1 COMMENT 'Số lượng',
    added_at DATETIME NOT NULL,
    
    FOREIGN KEY (cart_id) REFERENCES shopping_carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
) COMMENT 'Sản phẩm trong giỏ hàng';

-- WISHLIST (Giỏ hàng yêu thích) - MỚI
CREATE TABLE wishlists (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT 'Danh sách yêu thích của user';

CREATE TABLE wishlist_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wishlist_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL COMMENT 'Sản phẩm yêu thích',
    variant_id BIGINT COMMENT 'Variant cụ thể (nếu có)',
    added_at DATETIME NOT NULL,
    
    FOREIGN KEY (wishlist_id) REFERENCES wishlists(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE,
    UNIQUE KEY unique_wishlist_item (wishlist_id, product_id, variant_id)
) COMMENT 'Sản phẩm trong danh sách yêu thích';

-- ═══════════════════════════════════════════════════════════════
-- 4. VOUCHER & KHUYẾN MÃI
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE vouchers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Mã voucher: WELCOME10, FLASH50K',
    description VARCHAR(255),
    discount_type ENUM('PERCENTAGE', 'FIXED_AMOUNT') NOT NULL COMMENT 'Giảm % hoặc số tiền',
    discount_value DECIMAL(10, 2) NOT NULL COMMENT 'Giá trị giảm',
    max_discount_amount DECIMAL(10, 2) COMMENT 'Giảm tối đa (cho PERCENTAGE)',
    min_order_amount DECIMAL(10, 2) DEFAULT 0 COMMENT 'Giá trị đơn hàng tối thiểu',
    
    usage_limit INT COMMENT 'Số lần dùng tối đa (NULL = không giới hạn)',
    usage_per_user INT DEFAULT 1 COMMENT 'Số lần dùng/user',
    used_count INT DEFAULT 0 COMMENT 'Đã dùng bao nhiêu lần',
    
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) COMMENT 'Mã giảm giá';

CREATE TABLE user_vouchers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    voucher_id BIGINT NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    used_at DATETIME,
    order_id BIGINT COMMENT 'Đơn hàng đã sử dụng voucher',
    received_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE
) COMMENT 'Voucher của từng user';

-- ═══════════════════════════════════════════════════════════════
-- 5. ĐƠN HÀNG
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) UNIQUE NOT NULL COMMENT 'Mã đơn hàng: ORD-20250102-001',
    user_id BIGINT,
    
    order_date DATETIME NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'DELIVERED', 'CANCELLED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    
    -- Giá tiền
    subtotal DECIMAL(12, 2) NOT NULL COMMENT 'Tổng tiền hàng',
    shipping_fee DECIMAL(10, 2) DEFAULT 0 COMMENT 'Phí vận chuyển',
    discount_amount DECIMAL(12, 2) DEFAULT 0 COMMENT 'Giảm giá từ voucher',
    total_amount DECIMAL(12, 2) NOT NULL COMMENT 'Tổng thanh toán',
    
    voucher_id BIGINT COMMENT 'Voucher đã dùng',
    
    -- Thông tin giao hàng (snapshot)
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(255),
    customer_phone VARCHAR(20) NOT NULL,
    shipping_address VARCHAR(500) NOT NULL,
    shipping_city VARCHAR(100),
    shipping_district VARCHAR(100),
    shipping_ward VARCHAR(100),
    
    -- Thanh toán
    payment_method ENUM('COD', 'BANK_TRANSFER', 'CREDIT_CARD', 'MOMO', 'VNPAY') NOT NULL COMMENT 'Phương thức thanh toán',
    payment_status ENUM('PENDING', 'PAID', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    paid_at DATETIME COMMENT 'Thời điểm thanh toán',
    
    -- Ghi chú
    customer_note TEXT COMMENT 'Ghi chú của khách',
    admin_note TEXT COMMENT 'Ghi chú nội bộ',
    
    -- Vận chuyển
    shipping_carrier VARCHAR(100) COMMENT 'Đơn vị vận chuyển: GHTK, GHN',
    tracking_number VARCHAR(100) COMMENT 'Mã vận đơn',
    
    cancelled_at DATETIME,
    cancelled_reason TEXT,
    
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(50) COMMENT 'Admin cập nhật',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE SET NULL
) COMMENT 'Đơn hàng';

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT,
    variant_id BIGINT,
    
    -- Snapshot thông tin
    product_name VARCHAR(255) NOT NULL,
    variant_details VARCHAR(500) COMMENT 'Size: M, Màu: Đỏ',
    product_image VARCHAR(500) COMMENT 'Ảnh sản phẩm lúc mua',
    
    quantity INT NOT NULL,
    price_per_unit DECIMAL(10, 2) NOT NULL COMMENT 'Giá tại thời điểm mua',
    subtotal DECIMAL(12, 2) NOT NULL COMMENT 'Thành tiền',
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE SET NULL
) COMMENT 'Chi tiết sản phẩm trong đơn hàng';

CREATE TABLE order_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    note TEXT COMMENT 'Ghi chú thay đổi',
    changed_by VARCHAR(100) COMMENT 'Người thực hiện',
    changed_at DATETIME NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) COMMENT 'Lịch sử thay đổi trạng thái đơn hàng';

-- ═══════════════════════════════════════════════════════════════
-- 6. ĐÁNH GIÁ & BÌNH LUẬN
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    variant_id BIGINT COMMENT 'Variant được đánh giá',
    user_id BIGINT NOT NULL,
    order_id BIGINT COMMENT 'Đơn hàng liên quan',
    
    rating INT NOT NULL COMMENT 'Số sao 1-5',
    title VARCHAR(255) COMMENT 'Tiêu đề đánh giá',
    comment TEXT COMMENT 'Nội dung',
    
    is_verified_purchase BOOLEAN DEFAULT FALSE COMMENT 'Đã mua hàng',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    
    helpful_count INT DEFAULT 0 COMMENT 'Số lượt "hữu ích"',
    
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    CHECK (rating >= 1 AND rating <= 5)
) COMMENT 'Đánh giá sản phẩm';

CREATE TABLE review_media (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_id BIGINT NOT NULL,
    media_type ENUM('IMAGE', 'VIDEO') NOT NULL,
    media_url VARCHAR(500) NOT NULL COMMENT 'URL từ Cloud Storage',
    thumbnail_url VARCHAR(500) COMMENT 'Thumbnail video',
    file_size BIGINT COMMENT 'Kích thước file (bytes)',
    duration INT COMMENT 'Thời lượng video (giây)',
    display_order INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
) COMMENT 'Ảnh/video trong đánh giá';

CREATE TABLE review_replies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reply_text TEXT NOT NULL,
    is_admin_reply BOOLEAN DEFAULT FALSE COMMENT 'Phản hồi từ shop',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT 'Trả lời đánh giá';

CREATE TABLE review_helpful (
    user_id BIGINT,
    review_id BIGINT,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (user_id, review_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
) COMMENT 'User đánh dấu review hữu ích';

-- ═══════════════════════════════════════════════════════════════
-- 7. THÔNG BÁO
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('ORDER', 'PROMOTION', 'REVIEW', 'SYSTEM') NOT NULL,
    link VARCHAR(500) COMMENT 'Link đến chi tiết',
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT 'Thông báo cho user';

-- ═══════════════════════════════════════════════════════════════
-- 8. INDEX TỐI ƯU
-- ═══════════════════════════════════════════════════════════════

-- Products
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_slug ON products(slug);
CREATE INDEX idx_products_active ON products(is_active);
CREATE INDEX idx_product_images_product ON product_images(product_id);
CREATE INDEX idx_product_images_primary ON product_images(is_primary);

-- Variants
CREATE INDEX idx_variants_product ON product_variants(product_id);
CREATE INDEX idx_variants_sku ON product_variants(sku);
CREATE INDEX idx_variant_images_variant ON variant_images(variant_id);

-- Cart & Wishlist
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_wishlist_items_wishlist ON wishlist_items(wishlist_id);
CREATE INDEX idx_wishlist_items_product ON wishlist_items(product_id);

-- Orders
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_date ON orders(order_date);
CREATE INDEX idx_orders_number ON orders(order_number);
CREATE INDEX idx_order_items_order ON order_items(order_id);

-- Reviews
CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);
CREATE INDEX idx_reviews_status ON reviews(status);
CREATE INDEX idx_review_media_review ON review_media(review_id);

-- Vouchers
CREATE INDEX idx_vouchers_code ON vouchers(code);
CREATE INDEX idx_vouchers_dates ON vouchers(start_date, end_date);

-- Notifications
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);

-- ═══════════════════════════════════════════════════════════════
-- 9. DỮ LIỆU MẪU
-- ═══════════════════════════════════════════════════════════════

-- Roles
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- Categories
INSERT INTO categories (name, description, is_active, created_at, updated_at) VALUES
('Quần áo', 'Thời trang nam nữ', TRUE, NOW(), NOW()),
('Điện tử', 'Thiết bị điện tử', TRUE, NOW(), NOW()),
('Âm thanh', 'Loa, tai nghe', TRUE, NOW(), NOW()),
('Phụ kiện', 'Phụ kiện thời trang', TRUE, NOW(), NOW());

-- Vouchers
INSERT INTO vouchers (code, description, discount_type, discount_value, max_discount_amount, min_order_amount, usage_limit, start_date, end_date, is_active, created_at, updated_at) VALUES
('WELCOME10', 'Giảm 10% đơn đầu tiên', 'PERCENTAGE', 10, 100000, 0, NULL, '2024-01-01', '2025-12-31', TRUE, NOW(), NOW()),
('FLASH50K', 'Giảm 50k cho đơn từ 500k', 'FIXED_AMOUNT', 50000, NULL, 500000, 1000, '2024-01-01', '2025-12-31', TRUE, NOW(), NOW()),
('FREESHIP', 'Miễn phí vận chuyển', 'FIXED_AMOUNT', 30000, NULL, 200000, NULL, '2024-01-01', '2025-12-31', TRUE, NOW(), NOW());

-- ═══════════════════════════════════════════════════════════════
-- VÍ DỤ SẢN PHẨM
-- ═══════════════════════════════════════════════════════════════

-- 1. Áo thun (có Size + Màu)
INSERT INTO products (name, slug, description, base_price, has_variants, category_id, brand, is_active, created_at, updated_at)
VALUES ('Áo Thun Basic Cotton', 'ao-thun-basic-cotton', 'Áo thun 100% cotton co giãn thoáng mát', 199000, TRUE, 1, 'Local Brand', TRUE, NOW(), NOW());

INSERT INTO product_images (product_id, image_url, is_primary, display_order, created_at) VALUES
(1, 'https://cloudinary.com/products/shirt-main.jpg', TRUE, 1, NOW()),
(1, 'https://cloudinary.com/products/shirt-detail1.jpg', FALSE, 2, NOW()),
(1, 'https://cloudinary.com/products/shirt-detail2.jpg', FALSE, 3, NOW());

INSERT INTO product_variants (product_id, size, color, color_code, price, stock_quantity, sku, is_active, created_at, updated_at) VALUES
(1, 'M', 'Đỏ', '#FF0000', 199000, 50, 'SHIRT-RED-M', TRUE, NOW(), NOW()),
(1, 'M', 'Xanh', '#0000FF', 199000, 30, 'SHIRT-BLUE-M', TRUE, NOW(), NOW()),
(1, 'L', 'Đỏ', '#FF0000', 199000, 40, 'SHIRT-RED-L', TRUE, NOW(), NOW()),
(1, 'L', 'Xanh', '#0000FF', 199000, 20, 'SHIRT-BLUE-L', TRUE, NOW(), NOW());

INSERT INTO variant_images (variant_id, image_url, is_primary, created_at) VALUES
(1, 'https://cloudinary.com/variants/shirt-red-m.jpg', TRUE, NOW()),
(2, 'https://cloudinary.com/variants/shirt-blue-m.jpg', TRUE, NOW());

-- 2. Loa Bluetooth (chỉ Màu)
INSERT INTO products (name, slug, description, base_price, has_variants, category_id, brand, is_active, created_at, updated_at)
VALUES ('Loa JBL Flip 5', 'loa-jbl-flip-5', 'Loa Bluetooth chống nước IPX7, pin 12h', 2490000, TRUE, 3, 'JBL', TRUE, NOW(), NOW());

INSERT INTO product_images (product_id, image_url, is_primary, display_order, created_at) VALUES
(2, 'https://cloudinary.com/products/speaker-main.jpg', TRUE, 1, NOW());

INSERT INTO product_variants (product_id, size, color, color_code, price, stock_quantity, sku, is_active, created_at, updated_at) VALUES
(2, NULL, 'Đen', '#000000', 2490000, 100, 'SPEAKER-BLACK', TRUE, NOW(), NOW()),
(2, NULL, 'Xanh dương', '#0000FF', 2490000, 80, 'SPEAKER-BLUE', TRUE, NOW(), NOW()),
(2, NULL, 'Đỏ', '#FF0000', 2490000, 60, 'SPEAKER-RED', TRUE, NOW(), NOW());

-- 3. Tivi (chỉ Inch)
INSERT INTO products (name, slug, description, base_price, has_variants, category_id, brand, is_active, created_at, updated_at)
VALUES ('Smart TV Samsung 4K', 'smart-tv-samsung-4k', 'Tivi Samsung Crystal UHD 4K, HDR', 12990000, TRUE, 2, 'Samsung', TRUE, NOW(), NOW());

INSERT INTO product_variants (product_id, size, color, screen_size, storage, price, stock_quantity, sku, is_active, created_at, updated_at) VALUES
(3, NULL, NULL, '43 inch', NULL, 9990000, 30, 'TV-43INCH', TRUE, NOW(), NOW()),
(3, NULL, NULL, '55 inch', NULL, 12990000, 25, 'TV-55INCH', TRUE, NOW(), NOW()),
(3, NULL, NULL, '65 inch', NULL, 18990000, 15, 'TV-65INCH', TRUE, NOW(), NOW());

-- 4. Sản phẩm đơn giản (không variants)
INSERT INTO products (name, slug, description, base_price, has_variants, category_id, brand, is_active, created_at, updated_at)
VALUES ('Chuột Gaming Logitech G102', 'chuot-gaming-logitech-g102', 'Chuột gaming RGB 8000 DPI', 399000, FALSE, 4, 'Logitech', TRUE, NOW(), NOW());

INSERT INTO product_images (product_id, image_url, is_primary, display_order, created_at) VALUES
(4, 'https://cloudinary.com/products/mouse-g102.jpg', TRUE, 1, NOW());