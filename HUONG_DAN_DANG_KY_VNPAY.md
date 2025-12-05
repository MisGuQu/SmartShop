# 📘 HƯỚNG DẪN ĐĂNG KÝ VNPAY MERCHANT

## 🔍 VẤN ĐỀ URL

### ❌ VNPay Sandbox KHÔNG CHẤP NHẬN localhost:
```
http://localhost:8080/api/payments/vnpay/return  ❌ KHÔNG ĐƯỢC
```

### ✅ GIẢI PHÁP: Dùng Public URL (Ngrok hoặc công cụ tương tự)

**VNPay Sandbox yêu cầu URL công khai (public URL), không chấp nhận localhost!**

## 📝 GIẢI THÍCH

1. **VNPay Sandbox yêu cầu URL công khai** - không thể dùng `localhost` hoặc `127.0.0.1`
2. **Cần dùng công cụ tạo tunnel** như Ngrok, Cloudflare Tunnel, hoặc Localtunnel
3. Trong code của bạn, endpoint callback là: `/api/payments/vnpay/return`
4. URL đầy đủ = Public URL + Endpoint callback

## 🚀 CÁC BƯỚC ĐĂNG KÝ VNPAY MERCHANT

### Bước 1: Truy cập trang đăng ký VNPay
- **Sandbox (Test):** https://sandbox.vnpayment.vn/
- **Production:** https://vnpay.vn/ (cần liên hệ VNPay để đăng ký)

### Bước 2: Tạo Public URL cho Localhost (BẮT BUỘC)

**⚠️ QUAN TRỌNG:** VNPay Sandbox không chấp nhận localhost, bạn PHẢI tạo public URL trước!

#### Cách 1: Dùng Ngrok (Khuyến nghị - Dễ nhất)

1. **Tải Ngrok:**
   - Truy cập: https://ngrok.com/download
   - Tải về và giải nén
   - Hoặc cài qua package manager:
     ```bash
     # Windows (với Chocolatey)
     choco install ngrok
     
     # Hoặc download trực tiếp từ website
     ```

2. **Đăng ký tài khoản Ngrok (Miễn phí):**
   - Truy cập: https://dashboard.ngrok.com/signup
   - Đăng ký tài khoản miễn phí
   - Lấy **Authtoken** từ dashboard

3. **Cấu hình Ngrok:**
   ```bash
   ngrok config add-authtoken YOUR_AUTH_TOKEN
   ```

4. **Khởi động ứng dụng Spring Boot:**
   ```bash
   mvn spring-boot:run
   # Hoặc chạy từ IDE
   ```

5. **Chạy Ngrok tunnel:**
   ```bash
   ngrok http 8080
   ```

6. **Lấy Public URL:**
   - Ngrok sẽ hiển thị URL dạng: `https://abc123.ngrok-free.app`
   - Copy URL này (ví dụ: `https://abc123.ngrok-free.app`)

#### Cách 2: Dùng Cloudflare Tunnel (Miễn phí, không giới hạn)

1. **Cài đặt cloudflared:**
   ```bash
   # Windows: Download từ https://github.com/cloudflare/cloudflared/releases
   # Hoặc dùng package manager
   ```

2. **Chạy tunnel:**
   ```bash
   cloudflared tunnel --url http://localhost:8080
   ```

3. **Lấy Public URL** từ output

#### Cách 3: Dùng Localtunnel (Không cần đăng ký)

```bash
# Cài đặt
npm install -g localtunnel

# Chạy tunnel
lt --port 8080
```

### Bước 3: Điền thông tin đăng ký VNPay

#### Thông tin cần điền:
- **Shop Name:** SmartShop (hoặc tên shop của bạn)
- **URL:** `https://abc123.ngrok-free.app/api/payments/vnpay/return` ⚠️ **QUAN TRỌNG**
  - Thay `abc123.ngrok-free.app` bằng URL bạn nhận được từ Ngrok
  - **LƯU Ý:** Phải có `/api/payments/vnpay/return` ở cuối!
- **Email:** Email của bạn (ví dụ: phuongnhi810204@gmail.com)
- **Password:** Mật khẩu tài khoản VNPay
- **Confirm Password:** Xác nhận mật khẩu
- **Captcha:** Nhập mã xác nhận

### Bước 3: Sau khi đăng ký thành công

Bạn sẽ nhận được:
- **TMN Code** (Terminal Code)
- **Hash Secret** (Secret Key)

### Bước 4: Cập nhật vào file `application.properties`

Mở file: `src/main/resources/application.properties`

**QUAN TRỌNG:** Có 2 cách cấu hình:

#### Option 1: Dùng biến môi trường (Khuyến nghị - Linh hoạt)

Giữ nguyên trong `application.properties`:
```properties
# VNPay
app.payment.vnpay.tmn-code=VNPAY_TMN_CODE  # ← Thay bằng TMN Code thật
app.payment.vnpay.hash-secret=VNPAY_HASH_SECRET  # ← Thay bằng Hash Secret thật
app.payment.vnpay.pay-url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
app.payment.vnpay.return-url=${app.web.base-url}/api/payments/vnpay/return
```

Nhưng cần cập nhật `app.web.base-url` thành URL Ngrok:
```properties
# APPLICATION
app.web.base-url=https://abc123.ngrok-free.app  # ← Thay bằng URL Ngrok của bạn
```

**⚠️ LƯU Ý:** Mỗi lần khởi động lại Ngrok, URL sẽ thay đổi (trừ khi dùng plan trả phí). Bạn cần:
- Cập nhật lại `app.web.base-url` mỗi lần URL Ngrok thay đổi
- Hoặc dùng Ngrok với domain tĩnh (plan trả phí)

#### Option 2: Cấu hình trực tiếp return-url

```properties
# VNPay
app.payment.vnpay.tmn-code=2QXUI4J4  # ← Thay bằng TMN Code thật
app.payment.vnpay.hash-secret=RAOCTZRMZOTOGGNQTHGJSWBNGZODAXGI  # ← Thay bằng Hash Secret thật
app.payment.vnpay.pay-url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
app.payment.vnpay.return-url=https://abc123.ngrok-free.app/api/payments/vnpay/return  # ← URL Ngrok đầy đủ
```

**Ví dụ sau khi cập nhật:**
```properties
app.payment.vnpay.tmn-code=2QXUI4J4
app.payment.vnpay.hash-secret=RAOCTZRMZOTOGGNQTHGJSWBNGZODAXGI
app.payment.vnpay.pay-url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
app.payment.vnpay.return-url=https://abc123.ngrok-free.app/api/payments/vnpay/return
```

## ⚠️ LƯU Ý QUAN TRỌNG

### 1. VNPay Sandbox KHÔNG CHẤP NHẬN localhost
- ❌ **KHÔNG thể dùng `localhost` hoặc `127.0.0.1`**
- ❌ **KHÔNG thể dùng `http://localhost:8080/api/payments/vnpay/return`**
- ✅ **BẮT BUỘC phải dùng Public URL** (Ngrok, Cloudflare Tunnel, etc.)

### 2. Giải pháp: Dùng Ngrok hoặc công cụ tương tự

#### Ngrok (Khuyến nghị - Dễ nhất)
- ✅ Miễn phí (có giới hạn)
- ✅ Dễ sử dụng
- ✅ Hỗ trợ HTTPS tự động
- ⚠️ URL thay đổi mỗi lần restart (trừ plan trả phí)

#### Cloudflare Tunnel
- ✅ Miễn phí, không giới hạn
- ✅ URL ổn định hơn
- ⚠️ Cần cài đặt thêm

#### Localtunnel
- ✅ Miễn phí, không cần đăng ký
- ⚠️ URL thay đổi mỗi lần
- ⚠️ Có thể không ổn định

### 3. Môi trường Production
- ❌ **KHÔNG thể dùng `localhost`**
- ✅ Cần URL công khai (public URL)
- ✅ Phải có domain thật (ví dụ: `https://yourdomain.com/api/payments/vnpay/return`)
- ✅ Deploy ứng dụng lên server (VPS, Cloud, Heroku, etc.)

### 4. Lưu ý về Ngrok URL

**Vấn đề:** URL Ngrok thay đổi mỗi lần restart (trừ plan trả phí)

**Giải pháp:**
1. **Dùng Ngrok với domain tĩnh** (plan trả phí - $8/tháng)
2. **Hoặc cập nhật lại URL trong VNPay mỗi lần restart Ngrok**
3. **Hoặc dùng Cloudflare Tunnel** (miễn phí, ổn định hơn)

## 🔧 KIỂM TRA TÍCH HỢP

Sau khi cấu hình xong, kiểm tra:

1. **Khởi động lại ứng dụng:**
   ```bash
   mvn spring-boot:run
   ```

2. **Test thanh toán:**
   - Tạo đơn hàng
   - Chọn phương thức thanh toán VNPay
   - Kiểm tra xem có redirect đến VNPay không
   - Sau khi thanh toán, kiểm tra callback có hoạt động không

3. **Kiểm tra log:**
   - Xem log trong console để kiểm tra lỗi
   - Kiểm tra database xem PaymentTransaction có được tạo không

## 📞 HỖ TRỢ

Nếu gặp vấn đề:
- **VNPay Support:** 1900 55 55 77
- **Email:** [email protected]
- **Tài liệu:** https://sandbox.vnpayment.vn/apis/

## 📋 CHECKLIST

- [ ] Đã cài đặt và cấu hình Ngrok (hoặc công cụ tương tự)
- [ ] Đã khởi động ứng dụng Spring Boot trên port 8080
- [ ] Đã chạy Ngrok tunnel: `ngrok http 8080`
- [ ] Đã lấy được Public URL từ Ngrok (ví dụ: `https://abc123.ngrok-free.app`)
- [ ] Đã nhập đúng URL vào form VNPay: `https://abc123.ngrok-free.app/api/payments/vnpay/return`
- [ ] Đã nhận được TMN Code từ VNPay
- [ ] Đã nhận được Hash Secret từ VNPay
- [ ] Đã cập nhật `application.properties` với:
  - [ ] TMN Code thật
  - [ ] Hash Secret thật
  - [ ] Return URL đúng (URL Ngrok + `/api/payments/vnpay/return`)
- [ ] Đã khởi động lại ứng dụng
- [ ] Đã test thanh toán thành công
- [ ] Đã kiểm tra callback hoạt động đúng

---

**Lưu ý:** File này chỉ dùng cho mục đích hướng dẫn. Thông tin thực tế có thể thay đổi theo chính sách của VNPay.

