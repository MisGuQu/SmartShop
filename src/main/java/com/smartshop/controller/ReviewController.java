package com.smartshop.controller;

import com.smartshop.dto.review.ReviewRequest;
import com.smartshop.dto.review.ReviewResponse;
import com.smartshop.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // 3️⃣1️⃣ + 3️⃣2️⃣ Tạo / cập nhật đánh giá (1-5 sao + media)
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> createOrUpdateReview(
            @RequestPart("productId") Long productId,
            @RequestPart("rating") Integer rating,
            @RequestPart(value = "comment", required = false) String comment,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws IOException {

        ReviewRequest req = new ReviewRequest();
        req.setProductId(productId);
        req.setRating(rating != null ? rating : 5);
        req.setComment(comment);
        req.setFiles(files);

        return ResponseEntity.ok(reviewService.createOrUpdateReview(req));
    }

    // 3️⃣3️⃣ Hiển thị review dưới trang sản phẩm
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }
}


