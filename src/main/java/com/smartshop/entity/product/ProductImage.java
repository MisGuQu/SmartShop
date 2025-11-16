package com.smartshop.entity.product;

import jakarta.persistence.*;
import lombok.*;
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
}