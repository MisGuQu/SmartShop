package com.smartshop.controller.admin;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartshop.dto.order.OrderAdminDTO;
import com.smartshop.dto.order.UpdateOrderStatusRequest;
import com.smartshop.entity.order.Order;
import com.smartshop.response.ApiResponse;
import com.smartshop.service.admin.AdminOrderService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderAdminDTO>>> getOrders(
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<OrderAdminDTO> orders = orderService.getOrders(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request) {

        orderService.updateStatus(id, request.getNewStatus(), request.getNote());
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật trạng thái thành công"));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportOrders(
            @RequestParam(required = false) Order.OrderStatus status,
            HttpServletResponse response) throws IOException {

        byte[] excel = orderService.exportToExcel(status);
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=orders.xlsx");
        return ResponseEntity.ok(excel);
    }
}
