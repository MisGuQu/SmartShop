package com.smartshop.repository;

import org.springframework.data.domain.Page;           
import org.springframework.data.domain.Pageable;       
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartshop.entity.order.Order;
import com.smartshop.entity.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    List<Order> findByStatus(OrderStatus status);
    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    Page<Order> findByStatusIn(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);
    List<Order> findByUserId(Long userId);

    // Statistics queries
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate >= :startDate AND o.status != 'CANCELLED' AND o.status != 'REFUNDED'")
    Double getTotalRevenueSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startDate")
    Long countOrdersSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT o FROM Order o WHERE o.orderDate >= :startDate AND o.orderDate < :endDate")
    List<Order> findOrdersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Search and filter queries
    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.customerPhone) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY o.orderDate DESC")
    List<Order> findOrdersWithFilters(@Param("status") OrderStatus status, 
                                     @Param("keyword") String keyword);

    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findAllOrderByOrderDateDesc();
}