package com.smartshop.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartshop.entity.ReviewMedia;
public interface ReviewMediaRepository extends JpaRepository<ReviewMedia, Long> {
    List<ReviewMedia> findByReviewIdOrderByDisplayOrderAsc(Long reviewId);
}