package com.smartshop.repository;

import com.smartshop.entity.payment.PaymentWebhook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentWebhookRepository extends JpaRepository<PaymentWebhook, Long> {
}


