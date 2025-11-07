package com.smartshop.controller;

import com.smartshop.entity.cart.Wishlist;
import com.smartshop.entity.user.User;
import com.smartshop.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping
    public ResponseEntity<Wishlist> getWishlist(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(wishlistService.getWishlistByUser(user.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<Wishlist> addToWishlist(@AuthenticationPrincipal User user,
                                                  @RequestParam Long productId,
                                                  @RequestParam(required = false) Long variantId) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(wishlistService.addToWishlist(user.getId(), productId, variantId));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Wishlist> removeFromWishlist(@AuthenticationPrincipal User user,
                                                       @PathVariable Long itemId) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(wishlistService.removeFromWishlist(user.getId(), itemId));
    }
}
