package com.smartshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartshop.entity.ReviewHelpful;
import com.smartshop.entity.ReviewHelpfulId;

public interface ReviewHelpfulRepository extends JpaRepository<ReviewHelpful, ReviewHelpfulId> {
    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);
    int countByReviewId(Long reviewId);
}
