package com.smartshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.smartshop.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
}