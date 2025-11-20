package com.smartshop.controller;

import com.smartshop.dto.order.*;
import com.smartshop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 2️⃣7️⃣ Lịch sử mua hàng (user hiện tại)
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<List<OrderSummaryResponse>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    // 2️⃣8️⃣ + 2️⃣9️⃣ Chi tiết đơn hàng + theo dõi trạng thái
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDetail(orderId));
    }

    // Cập nhật trạng thái đơn hàng (Admin) – chờ → giao → hoàn tất → hủy
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDetailResponse> updateStatus(@PathVariable Long orderId,
                                                            @RequestBody UpdateOrderStatusRequest req) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, req));
    }

    // Hủy đơn hàng (User) - chỉ cho phép hủy đơn của chính mình
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDetailResponse> cancelOrder(@PathVariable Long orderId,
                                                             @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, reason));
    }

    // Xác nhận nhận hàng (User) - chỉ cho phép xác nhận đơn của chính mình
    @PostMapping("/{orderId}/confirm-received")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDetailResponse> confirmReceived(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.confirmReceived(orderId));
    }
}


