package com.smartshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import com.smartshop.entity.OrderStatusHistory;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByOrderIdOrderByChangedAtDesc(Long orderId);
}
