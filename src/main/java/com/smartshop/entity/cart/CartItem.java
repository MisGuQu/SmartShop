package com.smartshop.entity.cart;

import jakarta.persistence.*;
import lombok.*;
import com.smartshop.entity.product.Product;
import com.smartshop.entity.product.ProductVariant;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private ShoppingCart cart;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Builder.Default
    @Column(nullable = false)
    private int quantity = 1;

    @Builder.Default
    @Column(name = "is_wishlist", nullable = false)
    private boolean wishlist = false;
}