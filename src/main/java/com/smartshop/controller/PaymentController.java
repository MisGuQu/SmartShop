package com.smartshop.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smartshop.dto.payment.CreatePaymentRequest;
import com.smartshop.dto.payment.PaymentUrlResponse;
import com.smartshop.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Tạo URL thanh toán VNPay
    @PostMapping("/vnpay/create")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentUrlResponse> createVNPay(@RequestBody CreatePaymentRequest req,
                                                          HttpServletRequest httpRequest) {
        return ResponseEntity.ok(paymentService.createVNPayPayment(req.getOrderId(), httpRequest));
    }

    // Tạo URL thanh toán MoMo (demo)
    @PostMapping("/momo/create")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentUrlResponse> createMoMo(@RequestBody CreatePaymentRequest req) {
        return ResponseEntity.ok(paymentService.createMoMoPayment(req.getOrderId()));
    }

    // VNPay return URL
    @GetMapping("/vnpay/return")
    public ResponseEntity<Map<String, String>> vnpayReturn(@RequestParam Map<String, String> allParams)
            throws JsonProcessingException {
        String status = paymentService.handleVNPayReturn(allParams);
        Map<String, String> result = new HashMap<>();
        result.put("status", status);
        return ResponseEntity.ok(result);
    }

    // MoMo return URL (demo)
    @GetMapping("/momo/return")
    public ResponseEntity<Map<String, String>> momoReturn(@RequestParam Map<String, String> allParams)
            throws JsonProcessingException {
        String status = paymentService.handleMoMoReturn(allParams);
        Map<String, String> result = new HashMap<>();
        result.put("status", status);
        return ResponseEntity.ok(result);
    }
}


