package com.smartshop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartshop.entity.cart.ShoppingCart;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    Optional<ShoppingCart> findByUserId(Long userId);
}
