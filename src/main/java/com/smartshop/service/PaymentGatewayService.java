package com.smartshop.service;

import com.smartshop.entity.enums.PaymentMethod;
import com.smartshop.entity.payment.PaymentTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class PaymentGatewayService {

    @Value("${payment.vnpay.endpoint:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnPayEndpoint;

    @Value("${payment.vnpay.tmn-code:}")
    private String vnPayTmnCode;

    @Value("${payment.vnpay.hash-secret:}")
    private String vnPayHashSecret;

    @Value("${payment.vnpay.return-url:http://localhost:8080/payment/vnpay/return}")
    private String vnPayReturnUrl;

    @Value("${payment.vnpay.locale:vn}")
    private String vnPayLocale;

    @Value("${payment.momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String momoEndpoint;

    @Value("${payment.momo.partner-code:}")
    private String momoPartnerCode;

    @Value("${payment.momo.access-key:}")
    private String momoAccessKey;

    @Value("${payment.momo.secret-key:}")
    private String momoSecretKey;

    @Value("${payment.momo.return-url:http://localhost:8080/payment/momo/return}")
    private String momoReturnUrl;

    @Value("${payment.momo.notify-url:http://localhost:8080/payment/momo/ipn}")
    private String momoNotifyUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final DateTimeFormatter VNP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String createRedirectUrl(PaymentTransaction transaction) {
        if (transaction.getMethod() == PaymentMethod.VNPAY) {
            return buildVnPayUrl(transaction);
        }
        if (transaction.getMethod() == PaymentMethod.MOMO) {
            return createMomoPayment(transaction);
        }
        throw new IllegalArgumentException("Unsupported payment method: " + transaction.getMethod());
    }

    private String buildVnPayUrl(PaymentTransaction transaction) {
        if (!StringUtils.hasText(vnPayTmnCode) || !StringUtils.hasText(vnPayHashSecret)) {
            throw new IllegalStateException("VNPay configuration is missing (tmn-code/hash-secret).");
        }
        double amount = transaction.getAmount() != null ? transaction.getAmount() : 0.0;
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPayTmnCode);
        params.put("vnp_Amount", String.valueOf(Math.round(amount * 100)));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", transaction.getTransactionNo());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + transaction.getOrder().getOrderNumber());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", vnPayLocale);
        params.put("vnp_ReturnUrl", vnPayReturnUrl);
        params.put("vnp_CreateDate", LocalDateTime.now().format(VNP_DATE_FORMAT));
        params.put("vnp_ExpireDate", LocalDateTime.now().plusMinutes(15).format(VNP_DATE_FORMAT));

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (hashData.length() > 0) {
                hashData.append('&');
                query.append('&');
            }
            hashData.append(key).append('=').append(value);
            query.append(key).append('=')
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }

        String secureHash = hmacSHA512(vnPayHashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return UriComponentsBuilder.fromHttpUrl(vnPayEndpoint)
                .query(query.toString())
                .build()
                .toUriString();
    }

    private String createMomoPayment(PaymentTransaction transaction) {
        if (!StringUtils.hasText(momoPartnerCode) || !StringUtils.hasText(momoAccessKey) || !StringUtils.hasText(momoSecretKey)) {
            throw new IllegalStateException("MoMo configuration is missing (partner/access/secret key).");
        }
        double amount = transaction.getAmount() != null ? transaction.getAmount() : 0.0;
        String orderId = transaction.getTransactionNo();
        String requestId = transaction.getTransactionNo();
        String orderInfo = "Thanh toan don hang " + transaction.getOrder().getOrderNumber();

        String rawSignature = "accessKey=" + momoAccessKey +
                "&amount=" + Math.round(amount) +
                "&extraData=" +
                "&ipnUrl=" + momoNotifyUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + momoPartnerCode +
                "&redirectUrl=" + momoReturnUrl +
                "&requestId=" + requestId +
                "&requestType=captureWallet";

        String signature = hmacSHA256(momoSecretKey, rawSignature);

        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", momoPartnerCode);
        body.put("accessKey", momoAccessKey);
        body.put("requestId", requestId);
        body.put("amount", String.valueOf(Math.round(amount)));
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", momoReturnUrl);
        body.put("ipnUrl", momoNotifyUrl);
        body.put("extraData", "");
        body.put("requestType", "captureWallet");
        body.put("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        Map<?, ?> response = restTemplate.postForObject(momoEndpoint, request, Map.class);
        if (response != null && "0".equals(String.valueOf(response.get("resultCode")))) {
            Object payUrl = response.get("payUrl");
            if (payUrl != null) {
                return payUrl.toString();
            }
        }
        throw new IllegalStateException("Không thể tạo liên kết thanh toán MoMo: " +
                (response != null ? response.get("message") : "Không có phản hồi"));
    }

    private String hmacSHA512(String secret, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(keySpec);
            return toHex(hmac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể ký dữ liệu VNPay", ex);
        }
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKeySpec);
            return toHex(hmac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể ký dữ liệu MoMo", ex);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
 
