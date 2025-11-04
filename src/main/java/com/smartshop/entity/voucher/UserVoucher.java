package com.smartshop.entity.voucher;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.smartshop.entity.order.Order;
import com.smartshop.entity.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_vouchers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @Column(name = "is_used")
    private boolean isUsed = false;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @CreationTimestamp
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
}