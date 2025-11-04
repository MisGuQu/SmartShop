package com.smartshop.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartshop.dto.review.AdminReplyRequest;
import com.smartshop.dto.review.ReviewAdminDTO;
import com.smartshop.dto.review.ReviewReplyDTO;
import com.smartshop.entity.review.Review;
import com.smartshop.response.ApiResponse;
import com.smartshop.service.admin.AdminReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final AdminReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewAdminDTO>>> getReviews(
            @RequestParam(required = false) Review.ReviewStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ReviewAdminDTO> reviews = reviewService.getReviews(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long id) {
        reviewService.updateStatus(id, com.smartshop.entity.review.ReviewStatus.APPROVED);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã duyệt đánh giá"));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long id) {
        reviewService.updateStatus(id, com.smartshop.entity.review.ReviewStatus.REJECTED);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã từ chối đánh giá"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa đánh giá thành công"));
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<ApiResponse<ReviewReplyDTO>> reply(
            @PathVariable Long id,
            @RequestBody AdminReplyRequest request) {

        ReviewReplyDTO reply = reviewService.adminReply(id, request.getReplyText());
        return ResponseEntity.ok(ApiResponse.success(reply));
    }
}
