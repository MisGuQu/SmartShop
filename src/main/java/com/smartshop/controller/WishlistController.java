package com.smartshop.controller;

import com.smartshop.entity.cart.Wishlist;
import com.smartshop.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<Wishlist> getWishlist(@PathVariable Long userId) {
        return ResponseEntity.ok(wishlistService.getWishlistByUser(userId));
    }

    @PostMapping("/user/{userId}/items")
    public ResponseEntity<Wishlist> addToWishlist(@PathVariable Long userId,
                                                  @RequestParam Long productId,
                                                  @RequestParam(required = false) Long variantId) {
        return ResponseEntity.ok(wishlistService.addToWishlist(userId, productId, variantId));
    }

    @DeleteMapping("/user/{userId}/items/{itemId}")
    public ResponseEntity<Wishlist> removeFromWishlist(@PathVariable Long userId,
                                                       @PathVariable Long itemId) {
        return ResponseEntity.ok(wishlistService.removeFromWishlist(userId, itemId));
    }
}
