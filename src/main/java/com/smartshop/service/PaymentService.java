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

        // Tạo query string cho hash (không encode, loại bỏ vnp_SecureHash nếu có)
        String hashData = buildQueryForHash(params);
        String secureHash = hmacSHA512(vnpHashSecret, hashData);

        // Tạo query string cho URL (có encode)
        String query = buildQuery(params, false);
        String paymentUrl = vnpPayUrl + "?" + query + "&vnp_SecureHash=" + URLEncoder.encode(secureHash, StandardCharsets.UTF_8);
        
        // Log để debug (có thể xóa sau)
        System.out.println("VNPay Hash Data: " + hashData);
        System.out.println("VNPay Secure Hash: " + secureHash);
        System.out.println("VNPay Payment URL: " + paymentUrl);
        
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
    
    // Lấy orderId từ transaction number
    public Long getOrderIdByTransactionNo(String txnRef) {
        PaymentTransaction tx = paymentTransactionRepository.findByTransactionNo(txnRef)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return tx.getOrder().getId();
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
            ip = ip.split(",")[0].trim();
        } else {
            ip = request.getRemoteAddr();
        }
        
        // Convert IPv6 localhost to IPv4
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        
        return ip;
    }

    private String buildQuery(Map<String, String> params, boolean forHash) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            // Bỏ qua vnp_SecureHash và vnp_SecureHashType khi tính hash
            if (forHash && (entry.getKey().equals("vnp_SecureHash") || entry.getKey().equals("vnp_SecureHashType"))) {
                continue;
            }
            if (sb.length() > 0) sb.append('&');
            sb.append(entry.getKey()).append('=');
            if (forHash) {
                sb.append(entry.getValue() != null ? entry.getValue() : "");
            } else {
                sb.append(URLEncoder.encode(entry.getValue() != null ? entry.getValue() : "", StandardCharsets.UTF_8));
            }
        }
        return sb.toString();
    }
    
    // Tạo query string cho hash (theo chuẩn VNPay - giống code mẫu)
    private String buildQueryForHash(Map<String, String> params) {
        // Sắp xếp theo thứ tự alphabet (TreeMap đã tự động sắp xếp)
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        
        while (itr.hasNext()) {
            String fieldName = itr.next();
            
            // Bỏ qua vnp_SecureHash và vnp_SecureHashType
            if (fieldName.equals("vnp_SecureHash") || fieldName.equals("vnp_SecureHashType")) {
                continue;
            }
            
            String fieldValue = params.get(fieldName);
            
            // Chỉ thêm field nếu có giá trị (theo code mẫu VNPay)
            if (fieldValue != null && fieldValue.length() > 0) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
                
                // Chỉ thêm & nếu còn field tiếp theo (theo code mẫu VNPay)
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
        }
        
        return sb.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException("Key or data is null");
            }
            
            javax.crypto.Mac hmac = javax.crypto.Mac.getInstance("HmacSHA512");
            // Sử dụng key.getBytes() không chỉ định encoding (theo code mẫu VNPay)
            byte[] hmacKeyBytes = key.getBytes();
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac.init(secretKey);
            
            // Data sử dụng UTF-8 (theo code mẫu VNPay)
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac.doFinal(dataBytes);
            
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
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


