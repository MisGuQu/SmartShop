package com.smartshop.service;

import com.smartshop.entity.order.Order;
import com.smartshop.entity.order.OrderItem;
import com.smartshop.entity.user.User;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.dto.order.CheckoutRequest;
import com.smartshop.entity.enums.OrderStatus;
import com.smartshop.entity.enums.PaymentMethod;
import com.smartshop.entity.enums.PaymentStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public List<Order> getOrdersByUser(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order getOrderById(Long orderId) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    public Order createOrder(Long userId, Order payload) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(payload, "payload must not be null");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setShippingAddress(payload.getShippingAddress());
        order.setTotalAmount(payload.getTotalAmount() != null ? payload.getTotalAmount() : 0.0);
        order.setPaymentMethod(payload.getPaymentMethod() != null ? payload.getPaymentMethod() : PaymentMethod.COD);
        order.setPaymentStatus(payload.getPaymentStatus() != null ? payload.getPaymentStatus() : PaymentStatus.PENDING);
        order.setStatus(payload.getStatus() != null ? payload.getStatus() : OrderStatus.PENDING);

        if (payload.getItems() != null) {
            for (OrderItem item : payload.getItems()) {
                item.setOrder(order);
            }
            order.setItems(payload.getItems());
        }

        return orderRepository.save(order);
    }

    public Order createOrderFromCart(User user,
                                     CheckoutRequest request,
                                     double subtotal,
                                     double shippingFee,
                                     List<OrderItem> orderItems) {
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(request, "request must not be null");

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(subtotal + shippingFee);
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.COD);
        order.setPaymentStatus(PaymentStatus.PENDING);

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);

        return orderRepository.save(order);
    }

    public Order cancelOrder(Long orderId, String reason) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(status, "status must not be null");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        OrderStatus newStatus;
        try {
            // Convert SHIPPED to SHIPPING if needed
            String statusStr = status.toUpperCase();
            if ("SHIPPED".equals(statusStr)) {
                statusStr = "SHIPPING";
            }
            newStatus = OrderStatus.valueOf(statusStr);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }

        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    /**
     * Validate status transition
     * Valid flows: PENDING -> PROCESSING -> SHIPPING -> DELIVERED
     * Can cancel from PENDING or PROCESSING
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // No change
        }

        if (newStatus == OrderStatus.CANCELLED) {
            if (currentStatus != OrderStatus.PENDING && currentStatus != OrderStatus.PROCESSING) {
                throw new IllegalArgumentException("Chỉ có thể hủy đơn hàng ở trạng thái PENDING hoặc PROCESSING");
            }
            return;
        }

        // Normal flow validation
        switch (currentStatus) {
            case PENDING:
                if (newStatus != OrderStatus.PROCESSING && newStatus != OrderStatus.CANCELLED) {
                    throw new IllegalArgumentException("Đơn hàng PENDING chỉ có thể chuyển sang PROCESSING hoặc CANCELLED");
                }
                break;
            case PROCESSING:
                if (newStatus != OrderStatus.SHIPPING && newStatus != OrderStatus.CANCELLED) {
                    throw new IllegalArgumentException("Đơn hàng PROCESSING chỉ có thể chuyển sang SHIPPING hoặc CANCELLED");
                }
                break;
            case SHIPPING:
                if (newStatus != OrderStatus.DELIVERED) {
                    throw new IllegalArgumentException("Đơn hàng SHIPPING chỉ có thể chuyển sang DELIVERED");
                }
                break;
            case DELIVERED:
                throw new IllegalArgumentException("Đơn hàng DELIVERED không thể thay đổi trạng thái");
            case CANCELLED:
                throw new IllegalArgumentException("Đơn hàng CANCELLED không thể thay đổi trạng thái");
            case REFUNDED:
                throw new IllegalArgumentException("Đơn hàng REFUNDED không thể thay đổi trạng thái");
            case CONFIRMED:
                if (newStatus != OrderStatus.PROCESSING) {
                    throw new IllegalArgumentException("Đơn hàng CONFIRMED chỉ có thể chuyển sang PROCESSING");
                }
                break;
        }
    }

    /**
     * Lấy tất cả đơn hàng
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAllOrderByCreatedAtDesc();
    }

    /**
     * Tìm kiếm và lọc đơn hàng
     */
    public List<Order> searchAndFilterOrders(OrderStatus status, String keyword) {
        // Normalize keyword - convert empty string to null
        String normalizedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return orderRepository.findOrdersWithFilters(status, normalizedKeyword);
    }

    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
        return "ORD-" + LocalDateTime.now().getYear() + timestamp;
    }
}
