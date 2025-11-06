package com.smartshop.entity.product;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Builder.Default
    @Column(name = "is_primary")
    private boolean isPrimary = false;

    @Builder.Default
    @Column(name = "display_order")
    private int displayOrder = 0;

    @Column(name = "alt_text")
    private String altText;

    private Integer width;

    private Integer height;

    @Column(name = "format")
    private String format;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}