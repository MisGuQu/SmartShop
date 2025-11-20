package com.smartshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartshop.dto.payment.PaymentUrlResponse;
import com.smartshop.entity.order.Order;
import com.smartshop.entity.payment.PaymentTransaction;
import com.smartshop.entity.user.User;
import com.smartshop.entity.voucher.UserVoucher;
import com.smartshop.entity.voucher.Voucher;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.PaymentTransactionRepository;
import com.smartshop.repository.UserVoucherRepository;
import com.smartshop.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentService(OrderRepository orderRepository,
                          PaymentTransactionRepository paymentTransactionRepository,
                          VoucherRepository voucherRepository,
                          UserVoucherRepository userVoucherRepository) {
        this.orderRepository = orderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.voucherRepository = voucherRepository;
        this.userVoucherRepository = userVoucherRepository;
    }

    // VNPay config
    @Value("${app.payment.vnpay.tmn-code}")
    private String vnpTmnCode;
    @Value("${app.payment.vnpay.hash-secret}")
    private String vnpHashSecret;
    @Value("${app.payment.vnpay.pay-url}")
    private String vnpPayUrl;
    @Value("${app.payment.vnpay.return-url}")
    private String vnpReturnUrl;

    // MoMo config (demo - chưa gọi thật)
    @Value("${app.payment.momo.partner-code}")
    private String momoPartnerCode;

    // 1️⃣ Tạo giao dịch VNPay: trả về URL để redirect
    public PaymentUrlResponse createVNPayPayment(Long orderId, HttpServletRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        double amount = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        String txnRef = order.getOrderNumber(); // dùng orderNumber làm mã giao dịch

        // Tạo PaymentTransaction
        PaymentTransaction tx = PaymentTransaction.builder()
                .order(order)
                .method("VNPAY")
                .amount(amount)
                .status("PENDING")
                .transactionNo(txnRef)
                .build();
        paymentTransactionRepository.save(tx);

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpTmnCode);
        params.put("vnp_Amount", String.valueOf((long) (amount * 100))); // VNP yêu cầu nhân 100
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan don hang " + txnRef);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnpReturnUrl);
        params.put("vnp_IpAddr", getClientIp(request));
        params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        String query = buildQuery(params, false);
        String hashData = buildQuery(params, true);
        String secureHash = hmacSHA512(vnpHashSecret, hashData);

        String paymentUrl = vnpPayUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
        return new PaymentUrlResponse(paymentUrl);
    }

    // 2️⃣ Tạo giao dịch MoMo (demo: chỉ trả về URL giả để bạn dễ hiểu luồng)
    public PaymentUrlResponse createMoMoPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        double amount = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        String txnRef = order.getOrderNumber();

        PaymentTransaction tx = PaymentTransaction.builder()
                .order(order)
                .method("MOMO")
                .amount(amount)
                .status("PENDING")
                .transactionNo(txnRef)
                .build();
        paymentTransactionRepository.save(tx);

        // Thực tế: bạn sẽ gọi API MoMo để lấy payUrl. Ở đây trả về URL demo cho dễ hiểu.
        String demoUrl = "https://test-payment.momo.vn/pay?orderId=" + URLEncoder.encode(txnRef, StandardCharsets.UTF_8)
                + "&amount=" + (long) amount;
        return new PaymentUrlResponse(demoUrl);
    }

    // 3️⃣ Xử lý VNPay return URL
    public String handleVNPayReturn(Map<String, String> queryParams) throws JsonProcessingException {
        String txnRef = queryParams.get("vnp_TxnRef");
        String responseCode = queryParams.get("vnp_ResponseCode");

        PaymentTransaction tx = paymentTransactionRepository.findByTransactionNo(txnRef)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Lưu response thô
        tx.setGatewayResponse(objectMapper.writeValueAsString(queryParams));

        if ("00".equals(responseCode)) {
            tx.setStatus("SUCCESS");
            tx.getOrder().setPaymentStatus("PAID");
            markUserVoucherUsed(tx.getOrder());
        } else {
            tx.setStatus("FAILED");
            tx.getOrder().setPaymentStatus("FAILED");
        }

        paymentTransactionRepository.save(tx);
        return tx.getStatus();
    }

    // 4️⃣ Xử lý MoMo return URL (demo)
    public String handleMoMoReturn(Map<String, String> queryParams) throws JsonProcessingException {
        String orderId = queryParams.get("orderId");

        PaymentTransaction tx = paymentTransactionRepository.findByTransactionNo(orderId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        tx.setGatewayResponse(objectMapper.writeValueAsString(queryParams));

        String resultCode = queryParams.get("resultCode");
        if ("0".equals(resultCode)) {
            tx.setStatus("SUCCESS");
            tx.getOrder().setPaymentStatus("PAID");
            markUserVoucherUsed(tx.getOrder());
        } else {
            tx.setStatus("FAILED");
            tx.getOrder().setPaymentStatus("FAILED");
        }

        paymentTransactionRepository.save(tx);
        return tx.getStatus();
    }

    // Helpers
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip)) {
            return ip.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    private String buildQuery(Map<String, String> params, boolean forHash) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(entry.getKey()).append('=');
            if (forHash) {
                sb.append(entry.getValue());
            } else {
                sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
        }
        return sb.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac hmac = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating HMAC-SHA512", e);
        }
    }

    // Sau khi thanh toán thành công, đánh dấu voucher đã dùng cho user
    private void markUserVoucherUsed(Order order) {
        if (order.getVoucherCode() == null) return;
        if (order.getUser() == null) return;

        Voucher voucher = voucherRepository.findByCode(order.getVoucherCode())
                .orElse(null);
        if (voucher == null) return;

        userVoucherRepository.findByUserAndVoucher(order.getUser(), voucher)
                .ifPresent(uv -> {
                    uv.setUsed(true);
                    userVoucherRepository.save(uv);
                });
    }
}


