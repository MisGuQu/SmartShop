package com.smartshop.controller;

import com.smartshop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/vnpay/return")
    public String handleVnPayReturn(@RequestParam Map<String, String> params,
                                    RedirectAttributes redirectAttributes) {
        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String payload = buildPayload(params);

        if ("00".equals(responseCode)) {
            paymentService.markGatewaySuccess(txnRef, payload);
            redirectAttributes.addFlashAttribute("paymentMessage", "Thanh toán VNPay thành công.");
        } else {
            paymentService.markGatewayFailure(txnRef, payload);
            redirectAttributes.addFlashAttribute("paymentError", "Thanh toán VNPay thất bại (mã: " + responseCode + ")");
        }
        return "redirect:/payment/result";
    }

    @GetMapping("/momo/return")
    public String handleMoMoReturn(@RequestParam Map<String, String> params,
                                   RedirectAttributes redirectAttributes) {
        String orderId = params.get("orderId");
        String resultCode = params.get("resultCode");
        String payload = buildPayload(params);

        if ("0".equals(resultCode)) {
            paymentService.markGatewaySuccess(orderId, payload);
            redirectAttributes.addFlashAttribute("paymentMessage", "Thanh toán MoMo thành công.");
        } else {
            paymentService.markGatewayFailure(orderId, payload);
            redirectAttributes.addFlashAttribute("paymentError", "Thanh toán MoMo thất bại (mã: " + resultCode + ")");
        }
        return "redirect:/payment/result";
    }

    @PostMapping(value = "/momo/ipn", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> handleMoMoIpn(@RequestBody Map<String, Object> payload) {
        String orderId = payload.get("orderId") != null ? payload.get("orderId").toString() : null;
        String resultCode = payload.get("resultCode") != null ? payload.get("resultCode").toString() : "";
        String rawPayload = buildPayloadFromObjectMap(payload);

        Map<String, Object> response = new HashMap<>();
        try {
            if ("0".equals(resultCode)) {
                paymentService.markGatewaySuccess(orderId, rawPayload);
                response.put("resultCode", 0);
                response.put("message", "success");
            } else {
                paymentService.markGatewayFailure(orderId, rawPayload);
                response.put("resultCode", 1);
                response.put("message", "failed");
            }
        } catch (Exception ex) {
            response.put("resultCode", 1);
            response.put("message", "error");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/result")
    public String paymentResult() {
        return "payment/result";
    }

    private String buildPayload(Map<String, String> params) {
        StringJoiner joiner = new StringJoiner("&");
        params.forEach((key, value) -> joiner.add(key + "=" + value));
        return joiner.toString();
    }

    private String buildPayloadFromObjectMap(Map<String, Object> params) {
        StringJoiner joiner = new StringJoiner("&");
        params.forEach((key, value) -> joiner.add(key + "=" + (value != null ? value.toString() : "")));
        return joiner.toString();
    }
}
