package com.smartshop.service;

import com.smartshop.dto.order.CheckoutRequest;
import com.smartshop.entity.enums.OrderStatus;
import com.smartshop.entity.enums.PaymentMethod;
import com.smartshop.entity.enums.PaymentStatus;
import com.smartshop.entity.order.Order;
import com.smartshop.entity.order.OrderItem;
import com.smartshop.entity.user.User;
import com.smartshop.entity.voucher.Voucher;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.repository.VoucherRepository;
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
    private final VoucherRepository voucherRepository;

    public List<Order> getOrdersByUser(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
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

        Long voucherId = payload.getVoucher() != null ? payload.getVoucher().getId() : null;
        Voucher voucher = null;
        if (voucherId != null) {
            voucher = voucherRepository.findById(voucherId)
                    .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(defaultDouble(payload.getSubtotal()));
        order.setShippingFee(defaultDouble(payload.getShippingFee()));
        order.setDiscountAmount(defaultDouble(payload.getDiscountAmount()));
        order.setTotalAmount(payload.getTotalAmount() != null ? payload.getTotalAmount() : 0.0);
        order.setVoucher(voucher);
        order.setCustomerName(payload.getCustomerName());
        order.setCustomerEmail(payload.getCustomerEmail());
        order.setCustomerPhone(payload.getCustomerPhone());
        order.setShippingAddress(payload.getShippingAddress());
        order.setShippingCity(payload.getShippingCity());
        order.setShippingDistrict(payload.getShippingDistrict());
        order.setShippingWard(payload.getShippingWard());
        order.setPaymentMethod(payload.getPaymentMethod() != null ? payload.getPaymentMethod() : PaymentMethod.COD);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setCustomerNote(payload.getCustomerNote());

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
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(0.0);
        order.setTotalAmount(subtotal + shippingFee);
        order.setVoucher(null);
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingDistrict(request.getShippingDistrict());
        order.setShippingWard(request.getShippingWard());
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.COD);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setCustomerNote(request.getCustomerNote());
        order.setShippingCarrier(request.getShippingMethod() != null ? request.getShippingMethod().name() : null);

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
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelledReason(reason);
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(status, "status must not be null");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
        return "ORD-" + LocalDateTime.now().getYear() + timestamp;
    }

    private double defaultDouble(Double value) {
        return value != null ? value : 0.0;
    }
}
