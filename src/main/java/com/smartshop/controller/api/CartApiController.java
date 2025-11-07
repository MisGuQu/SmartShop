package com.smartshop.controller.api;

import com.smartshop.dto.cart.AddToCartRequest;
import com.smartshop.dto.cart.CartSummaryView;
import com.smartshop.dto.cart.UpdateCartItemRequest;
import com.smartshop.entity.user.User;
import com.smartshop.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Validated
public class CartApiController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartSummaryView> getCart(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(cartService.getCartSummary(user.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartSummaryView> addItem(@AuthenticationPrincipal User user,
                                                   @Valid @RequestBody AddToCartRequest request) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            cartService.addItem(user.getId(), request.getProductId(), request.getVariantId(), request.getQuantity());
            return ResponseEntity.status(HttpStatus.CREATED).body(cartService.getCartSummary(user.getId()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartSummaryView> updateItem(@AuthenticationPrincipal User user,
                                                      @PathVariable Long itemId,
                                                      @Valid @RequestBody UpdateCartItemRequest request) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            cartService.updateItem(user.getId(), itemId, request.getQuantity());
            return ResponseEntity.ok(cartService.getCartSummary(user.getId()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartSummaryView> removeItem(@AuthenticationPrincipal User user,
                                                      @PathVariable Long itemId) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            cartService.removeItem(user.getId(), itemId);
            return ResponseEntity.ok(cartService.getCartSummary(user.getId()));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}

