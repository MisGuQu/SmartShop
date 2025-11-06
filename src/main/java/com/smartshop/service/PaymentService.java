package com.smartshop.service;

import com.smartshop.entity.enums.PaymentStatus;
import com.smartshop.entity.enums.PaymentTransactionStatus;
import com.smartshop.entity.order.Order;
import com.smartshop.entity.payment.PaymentTransaction;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.PaymentTransactionRepository;
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
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;

    public PaymentTransaction createTransaction(Long orderId, PaymentTransaction payload) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(payload, "payload must not be null");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrder(order);
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setPaymentMethod(payload.getPaymentMethod());
        transaction.setAmount(payload.getAmount());
        transaction.setCurrency(payload.getCurrency() != null ? payload.getCurrency() : "VND");
        transaction.setCallbackUrl(payload.getCallbackUrl());
        transaction.setReturnUrl(payload.getReturnUrl());
        transaction.setStatus(PaymentTransactionStatus.PENDING);
        transaction.setIpAddress(payload.getIpAddress());
        transaction.setUserAgent(payload.getUserAgent());
        transaction.setRequestData(payload.getRequestData());

        return paymentTransactionRepository.save(transaction);
    }

    public PaymentTransaction getTransaction(Long transactionId) {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        return paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment transaction not found"));
    }

    public List<PaymentTransaction> getTransactionsByOrder(Long orderId) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        return paymentTransactionRepository.findByOrderId(orderId);
    }

    public PaymentTransaction markSuccess(Long transactionId) {
        PaymentTransaction transaction = getTransaction(transactionId);

        transaction.setStatus(PaymentTransactionStatus.SUCCESS);
        transaction.setUpdatedAt(LocalDateTime.now());

        Order order = transaction.getOrder();
        if (order != null) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }

        return paymentTransactionRepository.save(transaction);
    }

    public PaymentTransaction markFailure(Long transactionId, String reason) {
        PaymentTransaction transaction = getTransaction(transactionId);

        transaction.setStatus(PaymentTransactionStatus.FAILED);
        transaction.setErrorMessage(reason);
        transaction.setUpdatedAt(LocalDateTime.now());

        Order order = transaction.getOrder();
        if (order != null) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }

        return paymentTransactionRepository.save(transaction);
    }

    private String generateTransactionNo() {
        return "TXN-" + System.currentTimeMillis();
    }
}
