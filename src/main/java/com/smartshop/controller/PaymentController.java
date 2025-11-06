package com.smartshop.controller;

import com.smartshop.entity.payment.PaymentTransaction;
import com.smartshop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/order/{orderId}")
    public ResponseEntity<PaymentTransaction> createTransaction(@PathVariable Long orderId,
                                                                @RequestBody PaymentTransaction payload) {
        PaymentTransaction created = paymentService.createTransaction(orderId, payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentTransaction> getTransaction(@PathVariable Long transactionId) {
        return ResponseEntity.ok(paymentService.getTransaction(transactionId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentTransaction>> getTransactionsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getTransactionsByOrder(orderId));
    }

    @PostMapping("/{transactionId}/success")
    public ResponseEntity<PaymentTransaction> markSuccess(@PathVariable Long transactionId) {
        return ResponseEntity.ok(paymentService.markSuccess(transactionId));
    }

    @PostMapping("/{transactionId}/fail")
    public ResponseEntity<PaymentTransaction> markFailure(@PathVariable Long transactionId,
                                                          @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(paymentService.markFailure(transactionId, reason));
    }
}
