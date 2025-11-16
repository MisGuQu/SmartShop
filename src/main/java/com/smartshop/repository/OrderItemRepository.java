package com.smartshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartshop.entity.order.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    // Get top selling products in a month
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalQuantity, SUM(oi.price * oi.quantity) as totalRevenue " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.createdAt >= :startDate AND o.createdAt < :endDate " +
           "AND o.status NOT IN (com.smartshop.entity.enums.OrderStatus.CANCELLED, com.smartshop.entity.enums.OrderStatus.REFUNDED) " +
           "AND oi.product IS NOT NULL " +
           "GROUP BY oi.product.id, oi.product.name " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.id = :productId")
    List<OrderItem> findByProductId(@Param("productId") Long productId);
}