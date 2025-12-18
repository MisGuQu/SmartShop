# 🛍️ SmartShop - Hệ Thống Bán Hàng Online

Hệ thống thương mại điện tử (E-commerce) được xây dựng bằng Spring Boot, cung cấp đầy đủ các tính năng từ quản lý sản phẩm, giỏ hàng, đặt hàng đến thanh toán và đánh giá sản phẩm.

## 📋 Mục Lục

- [Tổng Quan](#tổng-quan)
- [Kiến Trúc Hệ Thống](#kiến-trúc-hệ-thống)
- [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
- [Tính Năng](#tính-năng)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
- [Cài Đặt và Chạy Ứng Dụng](#cài-đặt-và-chạy-ứng-dụng)
- [Cấu Hình](#cấu-hình)
- [Cấu Trúc Database](#cấu-trúc-database)
- [API Documentation](#api-documentation)
- [Tài Khoản Mặc Định](#tài-khoản-mặc-định)
- [Hướng Dẫn Sử Dụng](#hướng-dẫn-sử-dụng)
- [Tài Liệu Tham Khảo](#tài-liệu-tham-khảo)

---

## 🎯 Tổng Quan

SmartShop là một hệ thống bán hàng online hoàn chỉnh với các tính năng:

- **Frontend**: Thymeleaf templates với JavaScript vanilla
- **Backend**: Spring Boot REST API
- **Database**: MySQL
- **Authentication**: JWT + Spring Security
- **File Storage**: Cloudinary (ảnh/video)
- **Payment**: Tích hợp VNPay và MoMo (có thể mở rộng)

---

## 🏗️ Kiến Trúc Hệ Thống

Dự án sử dụng **REST API** được xây dựng theo **Mô hình 3 lớp (3-Layer Architecture)**:

```
┌─────────────────────────────────────┐
│   Controller Layer (Presentation)   │  ← REST API Endpoints
│   - AdminController.java            │     Trả về JSON responses
│   - AuthController.java             │
│   - ProductController.java          │
│   - CartController.java             │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Service Layer (Business)       │  ← Business Logic
│   - AdminService.java               │     Xử lý nghiệp vụ
│   - AuthService.java                │
│   - ProductService.java             │
│   - CartService.java                │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Repository Layer (Data Access)   │  ← Database Operations
│   - UserRepository.java             │     JPA/Hibernate
│   - ProductRepository.java          │
│   - OrderRepository.java            │
└──────────────┬──────────────────────┘
               │
         [MySQL Database]
```

### Đặc điểm kiến trúc:

- ✅ **REST API**: Tất cả endpoints trả về JSON, tuân thủ RESTful principles
- ✅ **3-Layer Architecture**: Tách biệt rõ ràng Controller → Service → Repository
- ✅ **Separation of Concerns**: Mỗi layer có trách nhiệm riêng biệt
- ✅ **Scalable**: Dễ dàng mở rộng và bảo trì
- ✅ **Testable**: Dễ dàng viết unit test cho từng layer

---

## 🛠️ Công Nghệ Sử Dụng

### Backend
- **Spring Boot 3.3.5** - Framework chính
- **Spring Security** - Bảo mật và xác thực
- **Spring Data JPA** - ORM và truy vấn database
- **JWT (JSON Web Token)** - Xác thực stateless
- **MySQL** - Database quan hệ
- **Thymeleaf** - Template engine cho frontend
- **Lombok** - Giảm boilerplate code

### Frontend
- **HTML5/CSS3** - Giao diện người dùng
- **JavaScript (Vanilla)** - Xử lý logic phía client
- **Responsive Design** - Tương thích mobile/desktop

### Dịch Vụ Bên Thứ Ba
- **Cloudinary** - Lưu trữ và quản lý ảnh/video
- **Gmail SMTP** - Gửi email (reset password, thông báo)
- **Google OAuth2** - Đăng nhập bằng Google
- **VNPay/MoMo** - Cổng thanh toán online

### Công Cụ Hỗ Trợ
- **Apache POI** - Xuất file Excel
- **iTextPDF** - Xuất hóa đơn PDF
- **Maven** - Quản lý dependencies

---

## ✨ Tính Năng

### 👤 Quản Lý Người Dùng
- ✅ Đăng ký/Đăng nhập tài khoản
- ✅ Đăng nhập bằng Google OAuth2
- ✅ Quên mật khẩu và reset qua email
- ✅ Quản lý profile (cập nhật thông tin, avatar)
- ✅ Phân quyền: Customer, Admin
- ✅ Quản lý người dùng (Admin)

### 🛍️ Quản Lý Sản Phẩm
- ✅ Xem danh sách sản phẩm
- ✅ Tìm kiếm sản phẩm (hỗ trợ không dấu)
- ✅ Lọc sản phẩm theo danh mục
- ✅ Chi tiết sản phẩm
- ✅ Quản lý sản phẩm (CRUD) - Admin
- ✅ Upload ảnh sản phẩm lên Cloudinary
- ✅ Quản lý tồn kho (stock)

### 📦 Quản Lý Danh Mục
- ✅ Xem danh sách danh mục
- ✅ Danh mục đa cấp (parent-child)
- ✅ CRUD danh mục - Admin

### 🛒 Giỏ Hàng & Wishlist
- ✅ Thêm/Xóa sản phẩm vào giỏ hàng
- ✅ Cập nhật số lượng
- ✅ Áp dụng voucher/ mã giảm giá
- ✅ Wishlist (yêu thích)
- ✅ Tính tổng tiền tự động

### 🎫 Voucher/Mã Giảm Giá
- ✅ Tạo và quản lý voucher - Admin
- ✅ Áp dụng voucher theo:
  - Phần trăm giảm giá (%)
  - Số tiền cố định
  - Điều kiện đơn hàng tối thiểu
  - Áp dụng theo danh mục sản phẩm
- ✅ Kiểm tra hạn sử dụng và điều kiện

### 📋 Đơn Hàng
- ✅ Tạo đơn hàng từ giỏ hàng
- ✅ Xem lịch sử đơn hàng
- ✅ Chi tiết đơn hàng
- ✅ Theo dõi trạng thái đơn hàng:
  - PENDING (Chờ xác nhận)
  - CONFIRMED (Đã xác nhận)
  - PROCESSING (Đang xử lý)
  - SHIPPING (Đang giao hàng)
  - DELIVERED (Đã giao hàng)
  - COMPLETED (Hoàn thành)
  - CANCELLED (Đã hủy)
- ✅ Hủy đơn hàng (nếu chưa xử lý)
- ✅ Xác nhận đã nhận hàng
- ✅ Quản lý đơn hàng - Admin

### 💳 Thanh Toán
- ✅ Thanh toán khi nhận hàng (COD)
- ✅ Thanh toán online:
  - VNPay
  - MoMo
- ✅ Lịch sử giao dịch
- ✅ Webhook callback từ cổng thanh toán

### ⭐ Đánh Giá Sản Phẩm
- ✅ Xem đánh giá của sản phẩm
- ✅ Tạo đánh giá (1-5 sao)
- ✅ Upload ảnh/video đánh giá
- ✅ Chỉ user đã mua mới được đánh giá
- ✅ Quản lý đánh giá - Admin

### 📊 Dashboard Admin
- ✅ Thống kê tổng quan:
  - Tổng số đơn hàng
  - Tổng doanh thu
  - Số lượng người dùng
  - Số lượng sản phẩm
- ✅ Quản lý người dùng
- ✅ Quản lý sản phẩm
- ✅ Quản lý đơn hàng
- ✅ Quản lý danh mục
- ✅ Quản lý voucher

### 📄 Hóa Đơn
- ✅ Xuất hóa đơn PDF
- ✅ Tải hóa đơn theo đơn hàng

### 🔔 Hệ Thống Thông Báo
- ✅ Thông báo tự động khi:
  - Đơn hàng thay đổi trạng thái (PENDING → CONFIRMED → SHIPPING → DELIVERED)
  - Thanh toán thành công/thất bại
  - Nhận voucher mới
  - Có đánh giá mới cho sản phẩm
- ✅ Xem danh sách thông báo
- ✅ Đếm số thông báo chưa đọc
- ✅ Đánh dấu đã đọc (từng thông báo hoặc tất cả)
- ✅ Phân loại thông báo theo type: ORDER, PAYMENT, PROMOTION, REVIEW, SYSTEM
- ✅ Thông báo có thể link đến đối tượng liên quan (order, voucher, etc.)

---

## 📁 Cấu Trúc Dự Án

```
smartshop/
├── src/
│   ├── main/
│   │   ├── java/com/smartshop/
│   │   │   ├── config/              # Cấu hình (Cloudinary, Security)
│   │   │   ├── controller/          # REST API Controllers
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CartController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── CheckoutController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── InvoiceController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── ReviewController.java
│   │   │   │   ├── VoucherController.java
│   │   │   │   └── ViewController.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── admin/
│   │   │   │   ├── auth/
│   │   │   │   ├── cart/
│   │   │   │   ├── category/
│   │   │   │   ├── common/
│   │   │   │   ├── order/
│   │   │   │   ├── payment/
│   │   │   │   ├── product/
│   │   │   │   ├── review/
│   │   │   │   └── voucher/
│   │   │   ├── entity/              # JPA Entities
│   │   │   │   ├── cart/
│   │   │   │   ├── enums/
│   │   │   │   ├── notification/
│   │   │   │   ├── order/
│   │   │   │   ├── payment/
│   │   │   │   ├── product/
│   │   │   │   ├── review/
│   │   │   │   ├── user/
│   │   │   │   └── voucher/
│   │   │   ├── exception/           # Exception handlers
│   │   │   ├── repository/          # JPA Repositories
│   │   │   ├── security/           # Security config (JWT, Security)
│   │   │   ├── service/            # Business logic
│   │   │   └── SmartshopApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       │   ├── css/            # Stylesheets
│   │       │   └── js/             # JavaScript files
│   │       └── templates/         # Thymeleaf templates
│   │           ├── admin/
│   │           ├── auth/
│   │           ├── cart/
│   │           ├── order/
│   │           ├── product/
│   │           ├── user/
│   │           └── wishlist/
│   └── test/                       # Unit tests
├── database.sql                    # Database schema và sample data
├── pom.xml                        # Maven dependencies
├── POSTMAN_API_GUIDE.md           # Hướng dẫn test API với Postman
├── QUICK_START_VNPAY.md            # Hướng dẫn nhanh tích hợp VNPay
├── DANH_SACH_FILE_VNPAY.md         # Danh sách file liên quan VNPay
└── README.md                      # File này
```

---

## 💻 Yêu Cầu Hệ Thống

- **Java**: JDK 21 trở lên
- **Maven**: 3.6+ 
- **MySQL**: 8.0+
- **IDE**: IntelliJ IDEA / Eclipse / VS Code (khuyến nghị)
- **Postman** (để test API)

### Dịch Vụ Bên Thứ Ba (Tùy chọn)
- **Cloudinary Account** (để upload ảnh/video)
- **Gmail Account** (để gửi email)
- **Google OAuth2 Credentials** (để đăng nhập Google)
- **VNPay/MoMo Account** (để thanh toán online)

---

## 🚀 Cài Đặt và Chạy Ứng Dụng

### Bước 1: Clone Repository

```bash
git clone <repository-url>
cd smartshop
```

### Bước 2: Cấu Hình Database

1. Tạo database MySQL:
```sql
CREATE DATABASE smartshop;
```

2. Hoặc chạy file SQL có sẵn:
```bash
mysql -u root -p < database.sql
```

> **Lưu ý**: Database name là `smartshop` (không phải `smartshop_db`)

### Bước 3: Cấu Hình Application Properties

Mở file `src/main/resources/application.properties` và cập nhật:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/smartshop?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
spring.datasource.username=root
spring.datasource.password=your_password

# Cloudinary (nếu muốn upload ảnh)
CLOUD_NAME=your_cloud_name
CLOUD_KEY=your_api_key
CLOUD_SECRET=your_api_secret

# Gmail SMTP (nếu muốn gửi email)
spring.mail.username=your_gmail@gmail.com
spring.mail.password=your_app_password

# Google OAuth2 (nếu muốn đăng nhập Google)
spring.security.oauth2.client.registration.google.client-id=your_client_id
spring.security.oauth2.client.registration.google.client-secret=your_client_secret

**Lưu ý quan trọng khi cấu hình Google OAuth:**
1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo OAuth 2.0 Client ID (nếu chưa có)
3. Trong phần "Authorized JavaScript origins", thêm:
   - `http://localhost:8080`
   - `http://127.0.0.1:8080` (nếu cần)
4. Trong phần "Authorized redirect URIs", thêm:
   - `http://localhost:8080` (cho Google Identity Services)
5. Đảm bảo Client ID trong `application.properties` và trong HTML templates (`register.html`, `login.html`) khớp nhau
```

### Bước 4: Build và Chạy Ứng Dụng

**Cách 1: Sử dụng Maven**

```bash
# Build project
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

**Cách 2: Sử dụng IDE**

1. Mở project trong IntelliJ IDEA / Eclipse
2. Đợi Maven download dependencies
3. Chạy class `SmartshopApplication.java`

### Bước 5: Truy Cập Ứng Dụng

- **Frontend**: http://localhost:8080
- **API Base URL**: http://localhost:8080/api

---

## ⚙️ Cấu Hình

### Database Configuration

File `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smartshop
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

### JWT Configuration

```properties
app.security.jwt.secret=U21hcnRTaG9wSldURGVmYXVsdFNlY3JldEtleTEyMzQ1Njc4OTA=
app.security.jwt.expiration=3600000  # 1 giờ
app.security.jwt.cookie-name=SMARTSHOP_TOKEN
```

### Cloudinary Configuration

1. Đăng ký tài khoản tại: https://cloudinary.com
2. Lấy credentials từ Dashboard → Settings → API Keys
3. Cập nhật trong `application.properties`:

```properties
CLOUD_NAME=your_cloud_name
CLOUD_KEY=your_api_key
CLOUD_SECRET=your_api_secret
```

### Email Configuration (Gmail)

1. Bật 2-Step Verification cho Gmail
2. Tạo App Password: https://myaccount.google.com/apppasswords
3. Cập nhật trong `application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_gmail@gmail.com
spring.mail.password=your_app_password
```

### VNPay Configuration

Để tích hợp thanh toán VNPay, xem hướng dẫn chi tiết trong file **[QUICK_START_VNPAY.md](QUICK_START_VNPAY.md)**

**Tóm tắt nhanh:**
1. Đăng ký tài khoản VNPay Sandbox: https://sandbox.vnpayment.vn/
2. Cài đặt Ngrok để tạo public URL (cho callback)
3. Cập nhật config trong `application.properties`:
```properties
app.payment.vnpay.tmn-code=YOUR_TMN_CODE
app.payment.vnpay.hash-secret=YOUR_HASH_SECRET
app.payment.vnpay.return-url=${app.web.base-url}/api/payments/vnpay/return
```

---

## 🗄️ Cấu Trúc Database

### Các Bảng Chính

- **users** - Thông tin người dùng
- **roles** - Vai trò (ROLE_USER, ROLE_CUSTOMER, ROLE_ADMIN)
- **users_roles** - Quan hệ user-role
- **categories** - Danh mục sản phẩm
- **products** - Sản phẩm
- **carts** - Giỏ hàng
- **cart_items** - Chi tiết giỏ hàng
- **orders** - Đơn hàng
- **order_items** - Chi tiết đơn hàng
- **order_status** - Lịch sử trạng thái đơn hàng
- **vouchers** - Mã giảm giá
- **user_vouchers** - Voucher của user
- **payment_transactions** - Giao dịch thanh toán
- **reviews** - Đánh giá sản phẩm
- **review_media** - Ảnh/video đánh giá
- **notifications** - Thông báo cho người dùng

Xem chi tiết trong file `database.sql`

---

## 📚 API Documentation

### Endpoints Chính

#### Authentication
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/forgot-password` - Quên mật khẩu
- `POST /api/auth/reset-password` - Reset mật khẩu
- `POST /api/auth/google` - Đăng nhập Google

#### Products (Public)
- `GET /api/products` - Danh sách sản phẩm
- `GET /api/products/{id}` - Chi tiết sản phẩm
- `GET /api/products?q=keyword` - Tìm kiếm
- `GET /api/products?categoryId=1` - Lọc theo danh mục

#### Cart (Cần token)
- `GET /api/cart` - Xem giỏ hàng
- `POST /api/cart/items` - Thêm vào giỏ
- `PUT /api/cart/items` - Cập nhật số lượng
- `DELETE /api/cart/items/{productId}` - Xóa khỏi giỏ
- `POST /api/cart/apply-voucher` - Áp dụng voucher

#### Orders (Cần token)
- `GET /api/orders/my` - Lịch sử đơn hàng
- `GET /api/orders/{id}` - Chi tiết đơn hàng
- `POST /api/checkout` - Tạo đơn hàng
- `PUT /api/orders/{id}/cancel` - Hủy đơn hàng

#### Reviews
- `GET /api/reviews/product/{productId}` - Xem đánh giá
- `POST /api/reviews` - Tạo đánh giá (multipart/form-data)

#### Notifications (Cần token)
- `GET /api/notifications` - Lấy danh sách thông báo của user
- `GET /api/notifications/unread-count` - Lấy số lượng thông báo chưa đọc
- `PUT /api/notifications/mark-all-read` - Đánh dấu tất cả là đã đọc
- `PUT /api/notifications/{id}/mark-read` - Đánh dấu một thông báo là đã đọc

#### Admin (Cần token ADMIN)
- `GET /api/admin/dashboard` - Thống kê
- `GET /api/admin/users` - Danh sách users
- `GET /api/admin/products` - Quản lý sản phẩm
- `GET /api/admin/orders` - Quản lý đơn hàng

**Xem chi tiết đầy đủ trong file `POSTMAN_API_GUIDE.md`**

### Payment Endpoints

#### VNPay (Cần token)
- `POST /api/payments/vnpay/create` - Tạo URL thanh toán VNPay
- `GET /api/payments/vnpay/return` - Callback từ VNPay (tự động)

**Xem hướng dẫn chi tiết**: [QUICK_START_VNPAY.md](QUICK_START_VNPAY.md)

---

## 👥 Tài Khoản Mặc Định

Sau khi chạy `database.sql`, có sẵn các tài khoản:

### Admin
- **Username**: `admin`
- **Email**: `admin123@gmail.com`
- **Password**: `123456`
- **Role**: ROLE_ADMIN

### Customer
- **Username**: `User123@gmail.com`
- **Password**: `User123@gmail.com` (hoặc mật khẩu đã hash trong DB)
- **Role**: ROLE_CUSTOMER

> **Lưu ý**: Mật khẩu trong database đã được hash bằng BCrypt. Nếu không đăng nhập được, hãy tạo user mới qua API register.

---

## 📖 Hướng Dẫn Sử Dụng

### 1. Đăng Ký/Đăng Nhập

1. Truy cập: http://localhost:8080/auth/register.html
2. Điền thông tin và đăng ký
3. Sau khi đăng ký, tự động đăng nhập và nhận JWT token

### 2. Mua Sắm

1. Xem danh sách sản phẩm: http://localhost:8080/product.html
2. Xem chi tiết sản phẩm: Click vào sản phẩm
3. Thêm vào giỏ hàng: Click "Thêm vào giỏ"
4. Xem giỏ hàng: http://localhost:8080/cart.html
5. Áp dụng voucher (nếu có)
6. Thanh toán: http://localhost:8080/checkout.html

### 3. Quản Lý Đơn Hàng

1. Xem lịch sử: http://localhost:8080/orders.html
2. Xem chi tiết: Click vào đơn hàng
3. Hủy đơn (nếu chưa xử lý)
4. Xác nhận đã nhận hàng

### 4. Đánh Giá Sản Phẩm

1. Vào đơn hàng đã giao
2. Click "Đánh giá"
3. Chọn sao, viết comment, upload ảnh/video
4. Submit

### 5. Admin Dashboard

1. Đăng nhập bằng tài khoản ADMIN
2. Truy cập: http://localhost:8080/admin/dashboard.html
3. Quản lý:
   - Sản phẩm: `/admin/products.html`
   - Danh mục: `/admin/categories.html`
   - Đơn hàng: `/admin/orders.html`
   - Người dùng: `/admin/users.html`
   - Voucher: `/admin/vouchers.html`

### 6. Thông Báo

1. Xem thông báo: Gọi API `GET /api/notifications`
2. Xem số thông báo chưa đọc: `GET /api/notifications/unread-count`
3. Đánh dấu đã đọc: `PUT /api/notifications/{id}/mark-read`
4. Đánh dấu tất cả đã đọc: `PUT /api/notifications/mark-all-read`

**Lưu ý**: Thông báo được tạo tự động khi:
- Đơn hàng thay đổi trạng thái
- Thanh toán thành công/thất bại
- Nhận voucher mới
- Có đánh giá mới

---

## 🧪 Testing

### Test API với Postman

1. Import collection từ file `POSTMAN_API_GUIDE.md`
2. Đăng nhập để lấy token
3. Thêm token vào Header: `Authorization: Bearer <token>`
4. Test các endpoints

### Test Frontend

1. Mở trình duyệt: http://localhost:8080
2. Test các tính năng:
   - Đăng ký/Đăng nhập
   - Xem sản phẩm
   - Thêm vào giỏ hàng
   - Tạo đơn hàng
   - Đánh giá sản phẩm

---

## 🔒 Bảo Mật

- ✅ JWT Authentication
- ✅ Password encryption (BCrypt)
- ✅ Role-based access control (RBAC)
- ✅ CORS configuration
- ✅ SQL injection prevention (JPA)
- ✅ XSS protection (Thymeleaf auto-escape)

---

## 📝 Ghi Chú

### Development Mode

- Thymeleaf cache: `false` (tự động reload)
- Hibernate DDL: `update` (tự động tạo/update tables)
- Logging: DEBUG mode cho development

### Production Mode

- Đổi `spring.jpa.hibernate.ddl-auto=validate`
- Bật Thymeleaf cache: `spring.thymeleaf.cache=true`
- Cấu hình HTTPS
- Sử dụng JWT secret mạnh hơn
- Cấu hình CORS cho domain thật

---

## 🤝 Đóng Góp

1. Fork project
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## 📄 License

Dự án này được phát triển cho mục đích học tập và nghiên cứu.

---

## 👨‍💻 Tác Giả

SmartShop Development Team

---

## 📚 Tài Liệu Tham Khảo

### Tài liệu trong dự án:

1. **[POSTMAN_API_GUIDE.md](POSTMAN_API_GUIDE.md)** - Hướng dẫn chi tiết test API với Postman
   - Tất cả endpoints với ví dụ request/response
   - Hướng dẫn upload file (Cloudinary)
   - Troubleshooting

2. **[QUICK_START_VNPAY.md](QUICK_START_VNPAY.md)** - Hướng dẫn nhanh tích hợp VNPay
   - Đăng ký VNPay Sandbox
   - Cấu hình Ngrok
   - Test thanh toán

3. **[DANH_SACH_FILE_VNPAY.md](DANH_SACH_FILE_VNPAY.md)** - Danh sách file liên quan VNPay
   - Cấu trúc file backend/frontend
   - Luồng hoạt động thanh toán

### Tài liệu bên ngoài:

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [JWT.io](https://jwt.io/) - JWT Debugger
- [VNPay Sandbox](https://sandbox.vnpayment.vn/)
- [Cloudinary Documentation](https://cloudinary.com/documentation)

---

## 📞 Liên Hệ

Nếu có thắc mắc hoặc cần hỗ trợ, vui lòng tạo issue trên repository.

---

**Chúc bạn sử dụng SmartShop thành công! 🎉**

