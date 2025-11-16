package com.smartshop.entity.product;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "variant_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Builder.Default
    @Column(name = "is_primary")
    private boolean isPrimary = false;
}