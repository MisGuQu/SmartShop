package com.smartshop.controller.api;

import com.smartshop.dto.review.CreateReviewRequest;
import com.smartshop.dto.review.ReviewDTO;
import com.smartshop.entity.User;
import com.smartshop.response.ApiResponse;
import com.smartshop.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * ✍️ Gửi đánh giá sản phẩm
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDTO>> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        ReviewDTO review = reviewService.createReview(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(review, "Gửi đánh giá thành công"));
    }

    /**
     * 💬 Lấy danh sách đánh giá đã được duyệt của sản phẩm (phân trang)
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ReviewDTO> reviews = reviewService.getApprovedReviews(productId, page, size);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
}
