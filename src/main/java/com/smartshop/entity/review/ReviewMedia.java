package com.smartshop.entity.review;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_media")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReviewMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(name = "media_url", nullable = false)
    private String mediaUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "file_size")
    private Long fileSize;

    private Integer duration;

    @Column(name = "display_order")
    private int displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

enum MediaType { IMAGE, VIDEO }