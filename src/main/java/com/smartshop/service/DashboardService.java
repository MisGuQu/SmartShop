package com.smartshop.service;

import com.smartshop.entity.enums.OrderStatus;
import com.smartshop.entity.enums.PaymentMethod;
import com.smartshop.repository.OrderItemRepository;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final VoucherRepository voucherRepository;

    /**
     * Lấy tổng doanh thu trong 7 ngày gần nhất
     */
    public Double getTotalRevenueLast7Days() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Double revenue = orderRepository.getTotalRevenueSince(sevenDaysAgo);
        return revenue != null ? revenue : 0.0;
    }

    /**
     * Lấy doanh thu theo từng ngày trong 7 ngày gần nhất
     */
    public Map<String, Double> getRevenueByDayLast7Days() {
        Map<String, Double> revenueByDay = new HashMap<>();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            
            List<com.smartshop.entity.order.Order> orders = orderRepository.findOrdersBetween(startOfDay, endOfDay.plusSeconds(1));
            double dayRevenue = orders.stream()
                    .filter(o -> o.getStatus() != com.smartshop.entity.enums.OrderStatus.CANCELLED 
                            && o.getStatus() != com.smartshop.entity.enums.OrderStatus.REFUNDED)
                    .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
                    .sum();
            
            revenueByDay.put(date.toString(), dayRevenue);
        }
        
        return revenueByDay;
    }

    /**
     * Lấy số lượng đơn hàng mới trong 24 giờ qua
     */
    public Long getNewOrdersLast24Hours() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        return orderRepository.countOrdersSince(twentyFourHoursAgo);
    }

    /**
     * Lấy top 5 sản phẩm bán chạy nhất trong tháng
     */
    public List<Map<String, Object>> getTop5BestSellingProductsThisMonth() {
        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        
        List<Object[]> results = orderItemRepository.findTopSellingProducts(startOfMonth, endOfMonth);
        
        List<Map<String, Object>> topProducts = new ArrayList<>();
        int limit = Math.min(5, results.size());
        
        for (int i = 0; i < limit; i++) {
            Object[] row = results.get(i);
            Map<String, Object> product = new HashMap<>();
            product.put("id", row[0]);
            product.put("name", row[1]);
            product.put("totalQuantity", row[2]);
            product.put("totalRevenue", row[3]);
            topProducts.add(product);
        }
        
        return topProducts;
    }

    /**
     * Lấy tổng hợp tất cả thống kê
     */
    public Map<String, Object> getAllStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenueLast7Days", getTotalRevenueLast7Days());
        stats.put("revenueByDayLast7Days", getRevenueByDayLast7Days());
        stats.put("newOrdersLast24Hours", getNewOrdersLast24Hours());
        stats.put("top5BestSellingProducts", getTop5BestSellingProductsThisMonth());
        stats.put("revenueByCategory", getRevenueByCategory());
        stats.put("revenueByPaymentMethod", getRevenueByPaymentMethod());
        stats.put("topCustomers", getTopCustomers());
        stats.put("voucherEffectiveness", getVoucherEffectiveness());
        return stats;
    }

    /**
     * Thống kê doanh thu theo danh mục
     */
    public List<Map<String, Object>> getRevenueByCategory() {
        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        
        List<com.smartshop.entity.order.Order> orders = orderRepository.findOrdersBetween(startOfMonth, endOfMonth);
        orders = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED && o.getStatus() != OrderStatus.REFUNDED)
                .collect(Collectors.toList());
        
        Map<Long, Map<String, Object>> categoryMap = new HashMap<>();
        
        for (com.smartshop.entity.order.Order order : orders) {
            for (com.smartshop.entity.order.OrderItem item : order.getItems()) {
                if (item.getProduct() != null && item.getProduct().getCategory() != null) {
                    Long categoryId = item.getProduct().getCategory().getId();
                    String categoryName = item.getProduct().getCategory().getName();

                    categoryMap.putIfAbsent(categoryId, new HashMap<>());
                    Map<String, Object> categoryData = categoryMap.get(categoryId);
                    categoryData.put("id", categoryId);
                    categoryData.put("name", categoryName);
                    double revenue = item.getPrice() != null ? item.getPrice() * item.getQuantity() : 0.0;
                    categoryData.put("revenue", ((Double) categoryData.getOrDefault("revenue", 0.0)) + revenue);
                    categoryData.put("quantity", ((Integer) categoryData.getOrDefault("quantity", 0)) + item.getQuantity());
                }
            }
        }
        
        return new ArrayList<>(categoryMap.values());
    }

    /**
     * Thống kê doanh thu theo phương thức thanh toán
     */
    public Map<String, Double> getRevenueByPaymentMethod() {
        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        
        List<com.smartshop.entity.order.Order> orders = orderRepository.findOrdersBetween(startOfMonth, endOfMonth);
        orders = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED && o.getStatus() != OrderStatus.REFUNDED)
                .collect(Collectors.toList());
        
        Map<String, Double> revenueByPayment = new HashMap<>();
        
        for (PaymentMethod method : PaymentMethod.values()) {
            revenueByPayment.put(method.name(), 0.0);
        }
        
        for (com.smartshop.entity.order.Order order : orders) {
            String method = order.getPaymentMethod().name();
            double revenue = revenueByPayment.getOrDefault(method, 0.0);
            revenue += order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
            revenueByPayment.put(method, revenue);
        }
        
        return revenueByPayment;
    }

    /**
     * Top khách hàng mua nhiều nhất
     */
    public List<Map<String, Object>> getTopCustomers() {
        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        
        List<com.smartshop.entity.order.Order> orders = orderRepository.findOrdersBetween(startOfMonth, endOfMonth);
        orders = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED && o.getStatus() != OrderStatus.REFUNDED)
                .collect(Collectors.toList());
        
        Map<Long, Map<String, Object>> customerMap = new HashMap<>();
        
        for (com.smartshop.entity.order.Order order : orders) {
            if (order.getUser() != null) {
                Long userId = order.getUser().getId();
                customerMap.putIfAbsent(userId, new HashMap<>());
                Map<String, Object> customerData = customerMap.get(userId);
                customerData.put("id", userId);
                customerData.put("name", order.getUser().getFullName());
                customerData.put("email", order.getUser().getEmail());
                customerData.put("totalSpent", ((Double) customerData.getOrDefault("totalSpent", 0.0)) + (order.getTotalAmount() != null ? order.getTotalAmount() : 0.0));
                customerData.put("orderCount", ((Integer) customerData.getOrDefault("orderCount", 0)) + 1);
            }
        }
        
        List<Map<String, Object>> topCustomers = new ArrayList<>(customerMap.values());
        topCustomers.sort((a, b) -> {
            Double spentA = (Double) a.get("totalSpent");
            Double spentB = (Double) b.get("totalSpent");
            return spentB.compareTo(spentA);
        });
        
        return topCustomers.stream().limit(10).collect(Collectors.toList());
    }

    /**
     * Hiệu quả của voucher khuyến mãi
     */
    public List<Map<String, Object>> getVoucherEffectiveness() {
        List<com.smartshop.entity.voucher.Voucher> vouchers = voucherRepository.findAll();

        return vouchers.stream().map(voucher -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("id", voucher.getId());
            stats.put("code", voucher.getCode());
            stats.put("type", voucher.getType());
            stats.put("value", voucher.getValue());
            stats.put("minOrder", voucher.getMinOrder());
            stats.put("startDate", voucher.getStartDate());
            stats.put("endDate", voucher.getEndDate());
            stats.put("isActive", voucher.isActive());
            return stats;
        }).collect(Collectors.toList());
    }
}

