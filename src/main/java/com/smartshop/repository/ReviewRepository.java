package com.smartshop.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartshop.entity.enums.ReviewStatus;
import com.smartshop.entity.review.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProduct_IdAndStatus(Long productId, ReviewStatus status, Pageable pageable);
    Page<Review> findByProduct_Id(Long productId, Pageable pageable);
    List<Review> findByUserId(Long userId);
    List<Review> findByOrderId(Long orderId);
    boolean existsByOrderIdAndProduct_Id(Long orderId, Long productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED'")
    Double getAverageRating(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED'")
    long countApprovedReviews(@Param("productId") Long productId);
}
