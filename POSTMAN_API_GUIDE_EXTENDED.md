# 📋 HƯỚNG DẪN CHI TIẾT API - PHẦN MỞ RỘNG

File này bổ sung hướng dẫn chi tiết cho các phần còn lại của POSTMAN_API_GUIDE.md

## 📋 8. ORDERS - HƯỚNG DẪN CHI TIẾT

### 8.1 Lịch sử mua hàng (Get My Orders)

**Mô tả:** Lấy danh sách tất cả đơn hàng của user hiện tại, sắp xếp theo thời gian tạo (mới nhất trước).

**Trong Postman:**
1. **Method:** `GET`
2. **URL:** `http://localhost:8080/api/orders/my`
3. **Headers:**
   - `Authorization: Bearer <token>`

**Response thành công (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "orderNumber": "ORD-ABC12345",
      "status": "PENDING",
      "totalAmount": 63000000.0,
      "paymentStatus": "PENDING",
      "paymentMethod": "COD",
      "createdAt": "2025-01-15T14:30:00"
    },
    {
      "id": 2,
      "orderNumber": "ORD-XYZ67890",
      "status": "COMPLETED",
      "totalAmount": 20000000.0,
      "paymentStatus": "PAID",
      "paymentMethod": "VNPAY",
      "createdAt": "2025-01-14T10:00:00"
    }
  ]
}
```

**Các trạng thái đơn hàng:**
- `PENDING`: Chờ xử lý
- `SHIPPING`: Đang giao hàng
- `COMPLETED`: Đã hoàn thành
- `CANCELLED`: Đã hủy

**Các trạng thái thanh toán:**
- `PENDING`: Chờ thanh toán
- `PAID`: Đã thanh toán
- `FAILED`: Thanh toán thất bại

---

### 8.2 Chi tiết đơn hàng (Get Order Detail)

**Mô tả:** Lấy thông tin chi tiết của một đơn hàng, bao gồm danh sách sản phẩm, lịch sử thay đổi trạng thái.

**Trong Postman:**
1. **Method:** `GET`
2. **URL:** `http://localhost:8080/api/orders/1`
   - ⚠️ **Thay `1` bằng `orderId` thực tế**
3. **Headers:**
   - `Authorization: Bearer <token>`

**Response thành công (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "orderNumber": "ORD-ABC12345",
    "status": "PENDING",
    "totalAmount": 63000000.0,
    "voucherCode": "SALE2025",
    "voucherDiscount": 7000000.0,
    "paymentMethod": "COD",
    "paymentStatus": "PENDING",
    "shippingAddress": "Nguyễn Văn A - 0123456789\n123 Đường ABC, Quận 1, TP.HCM",
    "items": [
      {
        "productId": 1,
        "productName": "iPhone 15 Pro",
        "price": 25000000.0,
        "quantity": 2,
        "lineTotal": 50000000.0
      }
    ],
    "statusHistory": [
      {
        "oldStatus": null,
        "newStatus": "PENDING",
        "createdAt": "2025-01-15T14:30:00"
      }
    ],
    "createdAt": "2025-01-15T14:30:00",
    "updatedAt": "2025-01-15T14:30:00"
  }
}
```

**Response lỗi (400 Bad Request):**
```json
{
  "success": false,
  "error": "Order not found"
}
```
hoặc
```json
{
  "success": false,
  "error": "Bạn không có quyền xem đơn hàng này"
}
```

**Lưu ý:**
- User chỉ có thể xem đơn hàng của chính mình
- Admin có thể xem tất cả đơn hàng

---

### 8.3 Cập nhật trạng thái đơn (ADMIN ONLY)

**Mô tả:** Admin cập nhật trạng thái đơn hàng (ví dụ: PENDING → SHIPPING → COMPLETED).

**Trong Postman:**
1. **Method:** `PUT`
2. **URL:** `http://localhost:8080/api/orders/1/status`
   - ⚠️ **Thay `1` bằng `orderId` thực tế**
3. **Headers:**
   - `Authorization: Bearer <admin_token>`
   - `Content-Type: application/json`
4. **Body:** Chọn `raw` → `JSON`, nhập:
```json
{
  "newStatus": "SHIPPING"
}
```

**Request Body Fields:**
- `newStatus` (String, required): Trạng thái mới (`PENDING`, `SHIPPING`, `COMPLETED`, `CANCELLED`)

**Response thành công (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "orderNumber": "ORD-ABC12345",
    "status": "SHIPPING",
    ...
    "statusHistory": [
      {
        "oldStatus": null,
        "newStatus": "PENDING",
        "createdAt": "2025-01-15T14:30:00"
      },
      {
        "oldStatus": "PENDING",
        "newStatus": "SHIPPING",
        "createdAt": "2025-01-15T15:00:00"
      }
    ]
  }
}
```

**Response lỗi (403 Forbidden):**
```json
{
  "success": false,
  "error": "Bạn không có quyền truy cập"
}
```

**Lưu ý:**
- ⚠️ **Chỉ ADMIN mới có quyền cập nhật trạng thái đơn hàng**
- Mỗi lần cập nhật sẽ được ghi vào `statusHistory`

---

## 👨‍💼 14. ADMIN MANAGEMENT (ADMIN ONLY)

### 14.1 Danh sách Users

**Mô tả:** Admin xem danh sách tất cả users trong hệ thống.

**Trong Postman:**
1. **Method:** `GET`
2. **URL:** `http://localhost:8080/api/admin/users`
3. **Headers:**
   - `Authorization: Bearer <admin_token>`

**Response thành công (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com",
      "fullName": "Test User",
      "phone": "0123456789",
      "isActive": true,
      "roles": ["ROLE_CUSTOMER"],
      "createdAt": "2025-01-15T10:00:00"
    }
  ]
}
```

---

### 14.2 Kích hoạt/Vô hiệu hóa User

**Mô tả:** Admin kích hoạt hoặc vô hiệu hóa tài khoản user.

**Trong Postman:**
1. **Method:** `PUT`
2. **URL:** `http://localhost:8080/api/admin/users/1/status?isActive=false`
   - ⚠️ **Thay `1` bằng `userId` thực tế**
   - Query param: `isActive=true` (kích hoạt) hoặc `isActive=false` (vô hiệu hóa)
3. **Headers:**
   - `Authorization: Bearer <admin_token>`

**Response thành công (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "testuser",
    "isActive": false,
    ...
  }
}
```

---

### 14.3 Danh sách Orders (Admin)

**Mô tả:** Admin xem tất cả đơn hàng trong hệ thống (không chỉ của mình).

**Trong Postman:**
1. **Method:** `GET`
2. **URL:** `http://localhost:8080/api/admin/orders`
3. **Headers:**
   - `Authorization: Bearer <admin_token>`

**Response:** Tương tự như 8.1, nhưng trả về tất cả đơn hàng

---

### 14.4 Danh sách Reviews (Admin)

**Mô tả:** Admin xem tất cả reviews trong hệ thống.

**Trong Postman:**
1. **Method:** `GET`
2. **URL:** `http://localhost:8080/api/admin/reviews`
3. **Headers:**
   - `Authorization: Bearer <admin_token>`

**Response thành công (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "productId": 1,
      "userId": 1,
      "rating": 5,
      "comment": "Sản phẩm rất tốt!",
      "mediaUrls": [...],
      "createdAt": "2025-01-15T16:00:00"
    }
  ]
}
```

---

### 14.5 Xóa Review (Admin)

**Mô tả:** Admin xóa review không phù hợp.

**Trong Postman:**
1. **Method:** `DELETE`
2. **URL:** `http://localhost:8080/api/admin/reviews/1`
   - ⚠️ **Thay `1` bằng `reviewId` thực tế**
3. **Headers:**
   - `Authorization: Bearer <admin_token>`

**Response thành công (200 OK):**
```json
{
  "success": true,
  "message": "Xóa review thành công",
  "data": null
}
```

---

## 📊 15. DASHBOARD & REPORTS (ADMIN ONLY)

### 15.1 Thống kê tổng quan

**Mô tả:** Lấy các số liệu thống kê tổng quan của hệ thống.

**Trong Postman:**
1. **Method:** `GET`
2. **URL:** `http://localhost:8080/api/admin/dashboard/stats`
3. **Headers:**
   - `Authorization: Bearer <admin_token>`

**Response thành công (200 OK):**
```json
{
  "success": true,
  "data": {
    "totalUsers": 150,
    "totalProducts": 500,
    "totalOrders": 1200,
    "totalRevenue": 5000000000.0,
    "pendingOrders": 50,
    "completedOrders": 1000
  }
}
```

---

### 15.2 Export Orders to Excel

**Mô tả:** Xuất danh sách đơn hàng ra file Excel.

**Trong Postman:**
1. **Method:** `GET`
2. **URL:** `http://localhost:8080/api/admin/dashboard/export/orders?startDate=2025-01-01T00:00:00&endDate=2025-01-31T23:59:59`
   - Query params (optional): `startDate`, `endDate` (format: `yyyy-MM-ddTHH:mm:ss`)
3. **Headers:**
   - `Authorization: Bearer <admin_token>`

**Response:** File Excel sẽ được download tự động

**Lưu ý:**
- Trong Postman, chọn tab "Send and Download" để lưu file
- File sẽ có tên `orders.xlsx`

---

### 15.3 Export Products to Excel

**Mô tả:** Xuất danh sách sản phẩm ra file Excel.

**Trong Postman:**
1. **Method:** `GET`
2. **URL:** `http://localhost:8080/api/admin/dashboard/export/products`
3. **Headers:**
   - `Authorization: Bearer <admin_token>`

**Response:** File Excel sẽ được download tự động (`products.xlsx`)

---

### 15.4 Export Users to Excel

**Mô tả:** Xuất danh sách users ra file Excel.

**Trong Postman:**
1. **Method:** `GET`
2. **URL:** `http://localhost:8080/api/admin/dashboard/export/users`
3. **Headers:**
   - `Authorization: Bearer <admin_token>`

**Response:** File Excel sẽ được download tự động (`users.xlsx`)

---

## 💡 TIPS & TRICKS

### 1. Sử dụng Environment Variables trong Postman

1. Tạo Environment: Click vào góc trên bên phải → "Manage Environments" → "Add"
2. Thêm biến: `token` = `<your_jwt_token>`
3. Sử dụng: Trong Header, dùng `{{token}}` thay vì paste token trực tiếp

### 2. Tạo Collection trong Postman

1. Tạo Collection mới: "SmartShop API"
2. Tạo folder cho từng module: Auth, Products, Cart, Orders, etc.
3. Import các request vào collection
4. Set Environment variable `base_url` = `http://localhost:8080`
5. Dùng `{{base_url}}/api/...` trong URL

### 3. Test Flow hoàn chỉnh

1. Register → Login → Lấy token
2. Tạo category (Admin)
3. Tạo product (Admin)
4. Upload ảnh product (Admin)
5. Thêm vào cart (User)
6. Áp dụng voucher (User)
7. Checkout (User)
8. Tạo payment (User)
9. Xem order (User)
10. Đánh giá sản phẩm (User)

---

**Chúc bạn test thành công! 🎉**

