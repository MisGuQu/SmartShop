package com.smartshop.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smartshop.dto.payment.CreatePaymentRequest;
import com.smartshop.dto.payment.PaymentUrlResponse;
import com.smartshop.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

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

    // VNPay return URL (for Link Payment - no signature verification needed)
    @GetMapping("/vnpay/return")
    public RedirectView vnpayReturn(@RequestParam Map<String, String> allParams)
            throws JsonProcessingException {
        try {
            String status = paymentService.handleVNPayReturn(allParams);
            
            // Lấy orderId từ transaction hoặc từ params
            String txnRef = allParams.get("vnp_TxnRef");
            Long orderId = null;
            
            if (txnRef != null && !txnRef.isEmpty()) {
                orderId = paymentService.getOrderIdByTransactionNo(txnRef);
            } else {
                // Fallback: lấy từ orderId param (nếu có)
                String orderIdStr = allParams.get("orderId");
                if (orderIdStr != null) {
                    orderId = Long.parseLong(orderIdStr);
                }
            }
            
            if (orderId == null) {
                // Nếu không tìm thấy orderId, redirect về trang chủ
                return new RedirectView("/?payment=error");
            }
            
            // Redirect về trang order detail
            String redirectUrl = "/order-detail.html?id=" + orderId;
            if ("SUCCESS".equals(status)) {
                redirectUrl += "&payment=success";
            } else {
                redirectUrl += "&payment=failed";
            }
            
            return new RedirectView(redirectUrl);
        } catch (Exception e) {
            // Log error và redirect về trang chủ
            System.err.println("Error handling VNPay return: " + e.getMessage());
            e.printStackTrace();
            return new RedirectView("/?payment=error");
        }
    }

}


