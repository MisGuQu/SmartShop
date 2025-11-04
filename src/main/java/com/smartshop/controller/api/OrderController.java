package com.smartshop.controller.api;

import com.smartshop.dto.order.CheckoutRequest;
import com.smartshop.dto.order.OrderDTO;
import com.smartshop.dto.order.OrderDetailDTO;
import com.smartshop.entity.user.User;
import com.smartshop.response.ApiResponse;
import com.smartshop.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class OrderController {

    private final OrderService orderService;

    /**
     * 🛒 Đặt hàng (tạo đơn hàng mới)
     */
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderDTO>> checkout(
            @Valid @RequestBody CheckoutRequest request,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        OrderDTO order = orderService.checkout(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(order, "Đặt hàng thành công"));
    }

    /**
     * 📦 Lấy danh sách đơn hàng của người dùng hiện tại (phân trang)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        Page<OrderDTO> orders = orderService.getMyOrders(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * 🔍 Xem chi tiết đơn hàng theo ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> getOrderDetail(
            @PathVariable Long orderId,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        OrderDetailDTO orderDetail = orderService.getOrderDetail(user.getId(), orderId);
        return ResponseEntity.ok(ApiResponse.success(orderDetail));
    }
}
