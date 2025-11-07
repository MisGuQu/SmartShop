package com.smartshop.repository;

import java.util.List;          
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartshop.entity.cart.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);

    @Query("SELECT ci FROM CartItem ci " +
            "WHERE ci.cart.id = :cartId " +
            "AND ci.product.id = :productId " +
            "AND ((:variantId IS NULL AND ci.variant IS NULL) OR (ci.variant.id = :variantId))")
    Optional<CartItem> findByCartProductAndVariant(@Param("cartId") Long cartId,
                                                   @Param("productId") Long productId,
                                                   @Param("variantId") Long variantId);
}