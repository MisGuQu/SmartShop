package com.smartshop.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartshop.entity.review.ReviewMedia;
public interface ReviewMediaRepository extends JpaRepository<ReviewMedia, Long> {
    List<ReviewMedia> findByReviewIdOrderByIdAsc(Long reviewId);
}