package com.smartshop.controller.admin;

import com.smartshop.dto.admin.DashboardDTO;
import com.smartshop.dto.admin.RevenueByDateDTO;
import com.smartshop.dto.response.ApiResponse;
import com.smartshop.service.admin.AdminDashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    /**
     * API: Lấy tổng quan dashboard
     * - Doanh thu hôm nay, tuần, tháng
     * - Số đơn hàng, khách hàng mới, sản phẩm bán chạy
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboard() {
        DashboardDTO data = dashboardService.getDashboardData();
        return ApiResponse.success(data, "Lấy dữ liệu dashboard thành công");
    }

    /**
     * API: Doanh thu theo ngày (từ - đến)
     * @param from Ngày bắt đầu (yyyy-MM-dd)
     * @param to   Ngày kết thúc (yyyy-MM-dd)
     */
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<List<RevenueByDateDTO>>> getRevenueByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<RevenueByDateDTO> revenue = dashboardService.getRevenueByDate(from, to);
        return ApiResponse.success(revenue, "Lấy doanh thu theo ngày thành công");
    }

    /**
     * API: Top 5 sản phẩm bán chạy (theo số lượng)
     */
    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<List<TopProductDTO>>> getTopProducts(
            @RequestParam(defaultValue = "7") int days) {
        List<TopProductDTO> topProducts = dashboardService.getTopSellingProducts(days);
        return ApiResponse.success(topProducts, "Lấy top sản phẩm thành công");
    }

    /**
     * API: Thống kê đơn hàng theo trạng thái
     */
    @GetMapping("/order-status")
    public ResponseEntity<ApiResponse<OrderStatusStatsDTO>> getOrderStatusStats() {
        OrderStatusStatsDTO stats = dashboardService.getOrderStatusStats();
        return ApiResponse.success(stats, "Lấy thống kê trạng thái đơn hàng thành công");
    }
}