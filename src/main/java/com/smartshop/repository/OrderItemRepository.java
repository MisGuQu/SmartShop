package com.smartshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartshop.entity.order.OrderItem;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
}