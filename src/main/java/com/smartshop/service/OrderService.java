package com.smartshop.service;

import com.smartshop.dto.order.*;
import com.smartshop.entity.order.Order;
import com.smartshop.entity.order.OrderStatusHistory;
import com.smartshop.entity.user.User;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.OrderStatusHistoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderStatusHistoryRepository statusHistoryRepository) {
        this.orderRepository = orderRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }

    // 2️⃣7️⃣ Lịch sử mua hàng (của user hiện tại)
    public List<OrderSummaryResponse> getMyOrders() {
        User user = getCurrentUser();
        return orderRepository.findByUser(user).stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .map(OrderSummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 2️⃣8️⃣ + 2️⃣9️⃣ Chi tiết đơn hàng + lịch sử trạng thái
    public OrderDetailResponse getOrderDetail(Long orderId) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Chỉ cho phép xem đơn của mình hoặc ADMIN
        if (order.getUser() != null && !order.getUser().getId().equals(user.getId())
                && user.getRoles().stream().noneMatch(r -> "ROLE_ADMIN".equals(r.getName()))) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        List<OrderStatusHistory> historyEntities = statusHistoryRepository.findByOrderOrderByCreatedAtAsc(order);
        List<OrderStatusHistoryResponse> history = historyEntities.stream()
                .map(OrderStatusHistoryResponse::fromEntity)
                .collect(Collectors.toList());

        return OrderDetailResponse.fromEntity(order, history);
    }

    // Cập nhật trạng thái (Admin) + ghi lịch sử
    public OrderDetailResponse updateStatus(Long orderId, UpdateOrderStatusRequest req) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String oldStatus = order.getStatus();
        String newStatus = req.getNewStatus();

        if (newStatus == null || newStatus.isBlank()) {
            throw new RuntimeException("Trạng thái mới không hợp lệ");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .build();
        statusHistoryRepository.save(history);

        List<OrderStatusHistoryResponse> historyResponses =
                statusHistoryRepository.findByOrderOrderByCreatedAtAsc(order).stream()
                        .map(OrderStatusHistoryResponse::fromEntity)
                        .collect(Collectors.toList());

        return OrderDetailResponse.fromEntity(order, historyResponses);
    }

    // Hủy đơn hàng (User) - chỉ cho phép hủy đơn của chính mình
    public OrderDetailResponse cancelOrder(Long orderId, String reason) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Kiểm tra quyền: chỉ cho phép hủy đơn của chính mình hoặc ADMIN
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
            if (!isAdmin) {
                throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
            }
        }

        // Kiểm tra trạng thái: chỉ cho phép hủy khi đơn ở trạng thái PENDING hoặc PROCESSING
        String currentStatus = order.getStatus();
        if (!"PENDING".equals(currentStatus) && !"PROCESSING".equals(currentStatus)) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng khi đơn ở trạng thái 'Chờ xử lý' hoặc 'Đang xử lý'");
        }

        // Cập nhật trạng thái thành CANCELLED
        String oldStatus = order.getStatus();
        order.setStatus("CANCELLED");
        orderRepository.save(order);

        // Ghi lịch sử
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus("CANCELLED")
                .build();
        statusHistoryRepository.save(history);

        // Lấy lại lịch sử đầy đủ
        List<OrderStatusHistoryResponse> historyResponses =
                statusHistoryRepository.findByOrderOrderByCreatedAtAsc(order).stream()
                        .map(OrderStatusHistoryResponse::fromEntity)
                        .collect(Collectors.toList());

        return OrderDetailResponse.fromEntity(order, historyResponses);
    }

    // Xác nhận nhận hàng (User) - chỉ cho phép xác nhận đơn của chính mình
    public OrderDetailResponse confirmReceived(Long orderId) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Kiểm tra quyền: chỉ cho phép xác nhận đơn của chính mình hoặc ADMIN
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
            if (!isAdmin) {
                throw new RuntimeException("Bạn không có quyền xác nhận đơn hàng này");
            }
        }

        // Kiểm tra trạng thái: chỉ cho phép xác nhận khi đơn ở trạng thái DELIVERED
        String currentStatus = order.getStatus();
        if (!"DELIVERED".equals(currentStatus)) {
            throw new RuntimeException("Chỉ có thể xác nhận nhận hàng khi đơn ở trạng thái 'Đã giao hàng'");
        }

        // Cập nhật trạng thái thành COMPLETED
        String oldStatus = order.getStatus();
        order.setStatus("COMPLETED");
        orderRepository.save(order);

        // Ghi lịch sử
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus("COMPLETED")
                .build();
        statusHistoryRepository.save(history);

        // Lấy lại lịch sử đầy đủ
        List<OrderStatusHistoryResponse> historyResponses =
                statusHistoryRepository.findByOrderOrderByCreatedAtAsc(order).stream()
                        .map(OrderStatusHistoryResponse::fromEntity)
                        .collect(Collectors.toList());

        return OrderDetailResponse.fromEntity(order, historyResponses);
    }
}


