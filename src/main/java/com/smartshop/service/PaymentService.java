package com.smartshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.smartshop.dto.payment.PaymentUrlResponse;
import com.smartshop.entity.order.Order;
import com.smartshop.entity.payment.PaymentTransaction;
import com.smartshop.entity.voucher.Voucher;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.PaymentTransactionRepository;
import com.smartshop.repository.UserVoucherRepository;
import com.smartshop.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Base64;

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
    
    // Post-construct: Trim HashSecret để tránh whitespace thừa
    @jakarta.annotation.PostConstruct
    public void init() {
        if (vnpHashSecret != null) {
            vnpHashSecret = vnpHashSecret.trim();
        }
        if (vnpTmnCode != null) {
            vnpTmnCode = vnpTmnCode.trim();
        }
    }

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

        // Tạo map params - TreeMap tự động sort alphabet
        // QUAN TRỌNG: Tất cả giá trị phải trim để tránh whitespace thừa
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpTmnCode != null ? vnpTmnCode.trim() : "");
        // Format số tiền: nhân 100, đảm bảo không có .0
        long amountInCents = (long) (amount * 100);
        params.put("vnp_Amount", String.valueOf(amountInCents));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef != null ? txnRef.trim() : "");
        // OrderInfo: trim whitespace để tránh space thừa (QUAN TRỌNG)
        String orderInfo = ("Thanh toan don hang " + txnRef).trim();
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        // ReturnUrl: PHẢI là raw, KHÔNG encode khi hash
        // QUAN TRỌNG: Đảm bảo returnUrl không có trailing slash hoặc whitespace thừa
        String returnUrlRaw = vnpReturnUrl != null ? vnpReturnUrl.trim() : "";
        // Loại bỏ trailing slash nếu có
        if (returnUrlRaw.endsWith("/")) {
            returnUrlRaw = returnUrlRaw.substring(0, returnUrlRaw.length() - 1);
        }
        params.put("vnp_ReturnUrl", returnUrlRaw);
        // IpAddr: trim whitespace để tránh space thừa
        String ipAddr = getClientIp(request);
        params.put("vnp_IpAddr", ipAddr != null ? ipAddr.trim() : "");
        params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        // Tạo query string cho hash (KHÔNG encode, đã sort, loại bỏ vnp_SecureHash và vnp_SecureHashType)
        // QUAN TRỌNG: Hash data phải là raw, không encode
        String hashData = buildQueryForHash(params);
        
        // Đảm bảo HashSecret không có whitespace thừa
        String hashSecretClean = vnpHashSecret != null ? vnpHashSecret.trim() : "";
        String secureHash = hmacSHA512(hashSecretClean, hashData);

        // Tạo query string cho URL (CÓ encode)
        // QUAN TRỌNG: Phải dùng CÙNG params và CÙNG thứ tự như hash data
        // Chỉ khác là encode giá trị khi build URL
        String query = buildQueryForUrl(params);
        
        // QUAN TRỌNG: Thêm vnp_SecureHashType=HmacSHA512 vào URL (BẮT BUỘC từ cuối 2023)
        // Field này KHÔNG được hash, chỉ thêm vào URL
        String paymentUrl = vnpPayUrl + "?" + query 
                + "&vnp_SecureHash=" + URLEncoder.encode(secureHash, StandardCharsets.UTF_8)
                + "&vnp_SecureHashType=HmacSHA512";
        
        // Tạo QR code từ payment URL
        String qrCodeBase64 = generateQRCode(paymentUrl);
        
        // Log để debug - CHI TIẾT để kiểm tra
        System.out.println("=== VNPay Payment Debug ===");
        System.out.println("HASH DATA (raw, no encode): [" + hashData + "]");
        System.out.println("HASH DATA length: " + hashData.length());
        System.out.println("SECURE HASH (before encode): " + secureHash);
        System.out.println("SECURE HASH length: " + secureHash.length());
        System.out.println("PAYMENT URL: " + paymentUrl);
        System.out.println("HashSecret (first 8 chars): " + (hashSecretClean != null && hashSecretClean.length() >= 8 ? hashSecretClean.substring(0, 8) + "..." : "NULL"));
        System.out.println("HashSecret length: " + (hashSecretClean != null ? hashSecretClean.length() : 0));
        System.out.println("HashSecret (full): [" + hashSecretClean + "]");
        System.out.println("TMN Code: " + vnpTmnCode);
        System.out.println("OrderInfo (raw): [" + orderInfo + "]");
        System.out.println("ReturnUrl (raw): [" + returnUrlRaw + "]");
        System.out.println("IpAddr (raw): [" + ipAddr + "]");
        System.out.println("Amount (cents): " + amountInCents);
        System.out.println("TxnRef: [" + txnRef + "]");
        System.out.println("Fields in hash: " + params.size() + " fields");
        System.out.println("QR Code generated: " + (qrCodeBase64 != null ? "Yes" : "No"));
        System.out.println("===========================");
        
        return new PaymentUrlResponse(paymentUrl, qrCodeBase64);
    }

    // 2️⃣ Xử lý VNPay return URL
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

    // Build query string cho URL (CÓ encode)
    private String buildQueryForUrl(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder sb = new StringBuilder();
        for (String fieldName : fieldNames) {
            // Bỏ qua vnp_SecureHash và vnp_SecureHashType
            if (fieldName.equals("vnp_SecureHash") || fieldName.equals("vnp_SecureHashType")) {
                continue;
            }
            
            String fieldValue = params.get(fieldName);
            
            // Chỉ thêm field nếu có giá trị
            if (fieldValue != null && fieldValue.length() > 0) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(fieldName).append("=");
                // ENCODE giá trị khi build URL
                sb.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
            }
        }
        return sb.toString();
    }
    
    // Tạo query string cho hash (theo chuẩn VNPay - KHÔNG encode, đã sort)
    // GIỐNG HỆT code demo VNPay: Config.hashAllFields()
    // QUAN TRỌNG: Logic phải GIỐNG HỆT code demo VNPay để tránh lỗi chữ ký
    private String buildQueryForHash(Map<String, String> params) {
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
            
            // Chỉ thêm field nếu có giá trị (GIỐNG HỆT code demo VNPay)
            // QUAN TRỌNG: KHÔNG encode khi tính hash (hash giá trị raw)
            if (fieldValue != null && fieldValue.length() > 0) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue); // Giá trị RAW, chưa encode
            }
            
            // QUAN TRỌNG: Logic này GIỐNG HỆT code demo VNPay
            // Thêm & nếu còn field tiếp theo (kể cả khi field hiện tại bị skip)
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        
        // Loại bỏ & thừa ở cuối (nếu có)
        String result = sb.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }
        
        return result;
    }

    // HMAC-SHA512
    private String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException("Key or data is null");
            }
            
            javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
            // Key dùng getBytes() KHÔNG có UTF-8
            byte[] hmacKeyBytes = key.getBytes();
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            
            // Data sử dụng UTF-8
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating HMAC-SHA512", e);
        }
    }

    // Tạo QR code từ URL (trả về base64)
    private String generateQRCode(String url) {
        try {
            int width = 300;
            int height = 300;
            
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, width, height, hints);
            
            // Convert to PNG image
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            // Convert to base64
            byte[] imageBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
            
        } catch (WriterException | IOException e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            e.printStackTrace();
            return null;
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
