package com.smartshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartshop.entity.WishlistItem;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByWishlistId(Long wishlistId);
    boolean existsByWishlistIdAndProductIdAndVariantId(Long wishlistId, Long productId, Long variantId);
    Optional<WishlistItem> findByWishlistIdAndProductIdAndVariantId(Long wishlistId, Long productId, Long variantId);
}
