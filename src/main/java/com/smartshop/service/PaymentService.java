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

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final PaymentGatewayService paymentGatewayService;

    public PaymentTransaction createTransaction(Long orderId, PaymentTransaction payload) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(payload, "payload must not be null");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrder(order);
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setMethod(payload.getMethod() != null ? payload.getMethod() : order.getPaymentMethod());
        transaction.setAmount(payload.getAmount() != null ? payload.getAmount() : order.getTotalAmount());
        transaction.setStatus(PaymentTransactionStatus.PENDING);
        transaction.setGatewayResponse(payload.getGatewayResponse());

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

    public String initiateGatewayPayment(Order order) {
        Objects.requireNonNull(order, "order must not be null");
        PaymentTransaction transaction = PaymentTransaction.builder()
                .order(order)
                .method(order.getPaymentMethod())
                .amount(order.getTotalAmount())
                .build();
        PaymentTransaction persisted = createTransaction(order.getId(), transaction);
        String gatewayUrl = paymentGatewayService.createRedirectUrl(persisted);
        persisted.setGatewayResponse(gatewayUrl);
        paymentTransactionRepository.save(persisted);
        return gatewayUrl;
    }

    public PaymentTransaction markSuccess(Long transactionId, String gatewayResponse) {
        PaymentTransaction transaction = getTransaction(transactionId);
        return updateTransactionStatus(transaction, PaymentTransactionStatus.SUCCESS, gatewayResponse);
    }

    public PaymentTransaction markFailure(Long transactionId, String reason, String gatewayResponse) {
        PaymentTransaction transaction = getTransaction(transactionId);
        String payload = gatewayResponse != null ? gatewayResponse : reason;
        return updateTransactionStatus(transaction, PaymentTransactionStatus.FAILED, payload);
    }

    public PaymentTransaction markGatewaySuccess(String transactionNo, String responseData) {
        PaymentTransaction transaction = paymentTransactionRepository.findByTransactionNo(transactionNo)
                .orElseThrow(() -> new EntityNotFoundException("Payment transaction not found"));
        return updateTransactionStatus(transaction, PaymentTransactionStatus.SUCCESS, responseData);
    }

    public PaymentTransaction markGatewayFailure(String transactionNo, String responseData) {
        PaymentTransaction transaction = paymentTransactionRepository.findByTransactionNo(transactionNo)
                .orElseThrow(() -> new EntityNotFoundException("Payment transaction not found"));
        return updateTransactionStatus(transaction, PaymentTransactionStatus.FAILED, responseData);
    }

    private PaymentTransaction updateTransactionStatus(PaymentTransaction transaction,
                                                       PaymentTransactionStatus status,
                                                       String responseData) {
        transaction.setStatus(status);
        if (responseData != null) {
            transaction.setGatewayResponse(responseData);
        }

        Order order = transaction.getOrder();
        if (order != null) {
            if (status == PaymentTransactionStatus.SUCCESS) {
                order.setPaymentStatus(PaymentStatus.PAID);
            } else if (status == PaymentTransactionStatus.FAILED) {
                order.setPaymentStatus(PaymentStatus.FAILED);
            }
            orderRepository.save(order);
        }

        return paymentTransactionRepository.save(transaction);
    }

    private String generateTransactionNo() {
        return "TXN-" + System.currentTimeMillis();
    }
}
