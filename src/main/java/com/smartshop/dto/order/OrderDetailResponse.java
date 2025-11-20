package com.smartshop.dto.order;

import com.smartshop.entity.order.Order;
import com.smartshop.entity.order.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class OrderDetailResponse {
    private Long id;
    private String orderNumber;
    private Double totalAmount;
    private String voucherCode;
    private Double voucherDiscount;
    private Double shippingFee;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private String shippingAddress;
    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryResponse> statusHistory;

    @Data
    @Builder
    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private Double price;
        private Integer quantity;
        private Double lineTotal;

        public static OrderItemResponse fromEntity(OrderItem item) {
            double price = item.getPrice();
            int qty = item.getQuantity();
            return OrderItemResponse.builder()
                    .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                    .productName(item.getProduct() != null ? item.getProduct().getName() : "N/A")
                    .price(price)
                    .quantity(qty)
                    .lineTotal(price * qty)
                    .build();
        }
    }

    public static OrderDetailResponse fromEntity(Order o, List<OrderStatusHistoryResponse> history) {
        List<OrderItemResponse> itemResponses = o.getItems().stream()
                .map(OrderItemResponse::fromEntity)
                .collect(Collectors.toList());

        // Calculate subtotal from items
        double subtotal = itemResponses.stream()
                .mapToDouble(item -> item.getLineTotal())
                .sum();
        
        // Calculate shipping fee: totalAmount - (subtotal - voucherDiscount)
        double voucherDiscount = o.getVoucherDiscount() != null ? o.getVoucherDiscount() : 0.0;
        double subtotalAfterDiscount = subtotal - voucherDiscount;
        double totalAmount = o.getTotalAmount() != null ? o.getTotalAmount() : 0.0;
        double shippingFee = Math.max(0, totalAmount - subtotalAfterDiscount);

        return OrderDetailResponse.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .totalAmount(totalAmount)
                .voucherCode(o.getVoucherCode())
                .voucherDiscount(voucherDiscount)
                .shippingFee(shippingFee)
                .status(o.getStatus())
                .paymentStatus(o.getPaymentStatus())
                .paymentMethod(o.getPaymentMethod())
                .shippingAddress(o.getShippingAddress())
                .createdAt(o.getCreatedAt())
                .items(itemResponses)
                .statusHistory(history)
                .build();
    }
}


