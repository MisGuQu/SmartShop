package com.smartshop.service;

import com.smartshop.entity.review.Review;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewService {

    public List<Review> getReviewsByProduct(Long productId) {
        // TODO: Lấy danh sách đánh giá theo sản phẩm
        return List.of();
    }

    public Review addReview(Long userId, Long productId, String content, int rating) {
        // TODO: Thêm đánh giá
        return new Review();
    }

    public void deleteReview(Long reviewId) {
        // TODO: Xóa đánh giá
    }

    public double getAverageRating(Long productId) {
        // TODO: Tính điểm trung bình đánh giá
        return 0.0;
    }
}
