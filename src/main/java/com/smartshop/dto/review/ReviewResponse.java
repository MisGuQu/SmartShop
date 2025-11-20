package com.smartshop.dto.review;

import com.smartshop.entity.review.Review;
import com.smartshop.entity.review.ReviewMedia;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long userId;
    private String username;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private List<String> mediaUrls;

    public static ReviewResponse fromEntity(Review review) {
        List<String> urls = review.getMedia().stream()
                .map(ReviewMedia::getUrl)
                .collect(Collectors.toList());

        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .username(review.getUser().getUsername())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .mediaUrls(urls)
                .build();
    }
}


