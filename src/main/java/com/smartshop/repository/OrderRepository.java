package com.smartshop.repository;

import org.springframework.data.domain.Page;           
import org.springframework.data.domain.Pageable;       
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartshop.entity.order.Order;
import com.smartshop.entity.order.Order.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    List<Order> findByStatus(OrderStatus status);
    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    Page<Order> findByStatusIn(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);
}