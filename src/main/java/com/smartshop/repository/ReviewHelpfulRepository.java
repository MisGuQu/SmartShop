package com.smartshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartshop.entity.review.ReviewHelpful;
import com.smartshop.entity.review.ReviewHelpfulId;

public interface ReviewHelpfulRepository extends JpaRepository<ReviewHelpful, ReviewHelpfulId> {
    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);
    int countByReviewId(Long reviewId);
}
