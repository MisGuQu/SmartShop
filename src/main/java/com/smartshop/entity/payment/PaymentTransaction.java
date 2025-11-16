package com.smartshop.entity.payment;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.smartshop.entity.enums.PaymentMethod;
import com.smartshop.entity.enums.PaymentTransactionStatus;
import com.smartshop.entity.order.Order;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "method")
    private PaymentMethod method;

    @Column(name = "amount")
    private Double amount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentTransactionStatus status = PaymentTransactionStatus.PENDING;

    @Column(name = "transaction_no")
    private String transactionNo;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
