package com.smartshop.entity.review;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.smartshop.entity.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_helpful")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@IdClass(ReviewHelpfulId.class)
public class ReviewHelpful {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}