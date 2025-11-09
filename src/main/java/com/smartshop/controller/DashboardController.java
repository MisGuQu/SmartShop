package com.smartshop.controller;

import com.smartshop.service.DashboardService;
import com.smartshop.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final ReportService reportService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAllStatistics() {
        Map<String, Object> stats = dashboardService.getAllStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/revenue/last-7-days")
    public ResponseEntity<Double> getTotalRevenueLast7Days() {
        Double revenue = dashboardService.getTotalRevenueLast7Days();
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/revenue/by-day")
    public ResponseEntity<Map<String, Double>> getRevenueByDayLast7Days() {
        Map<String, Double> revenueByDay = dashboardService.getRevenueByDayLast7Days();
        return ResponseEntity.ok(revenueByDay);
    }

    @GetMapping("/orders/last-24-hours")
    public ResponseEntity<Long> getNewOrdersLast24Hours() {
        Long count = dashboardService.getNewOrdersLast24Hours();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/products/top-5")
    public ResponseEntity<Map<String, Object>> getTop5BestSellingProducts() {
        Map<String, Object> response = Map.of(
            "products", dashboardService.getTop5BestSellingProductsThisMonth()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/revenue/by-category")
    public ResponseEntity<List<Map<String, Object>>> getRevenueByCategory() {
        return ResponseEntity.ok(dashboardService.getRevenueByCategory());
    }

    @GetMapping("/revenue/by-payment-method")
    public ResponseEntity<Map<String, Double>> getRevenueByPaymentMethod() {
        return ResponseEntity.ok(dashboardService.getRevenueByPaymentMethod());
    }

    @GetMapping("/customers/top")
    public ResponseEntity<List<Map<String, Object>>> getTopCustomers() {
        return ResponseEntity.ok(dashboardService.getTopCustomers());
    }

    @GetMapping("/vouchers/effectiveness")
    public ResponseEntity<List<Map<String, Object>>> getVoucherEffectiveness() {
        return ResponseEntity.ok(dashboardService.getVoucherEffectiveness());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(@RequestParam String format) {
        try {
            byte[] reportData;
            String contentType;
            String fileName;

            if ("excel".equalsIgnoreCase(format)) {
                reportData = reportService.exportExcel();
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                fileName = "bao-cao-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";
            } else if ("pdf".equalsIgnoreCase(format)) {
                reportData = reportService.exportPDF();
                contentType = "application/pdf";
                fileName = "bao-cao-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            } else {
                return ResponseEntity.badRequest().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(reportData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

