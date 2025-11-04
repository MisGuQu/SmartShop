package com.smartshop.entity.cart;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.smartshop.entity.product.Product;
import com.smartshop.entity.product.ProductVariant;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WishlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @CreationTimestamp
    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @Column(name = "unique_wishlist_item", unique = true)
    private String uniqueConstraint; // Tạm (xem chú thích)
}