package com.smartshop.service;

import com.smartshop.entity.cart.CartItem;
import com.smartshop.entity.cart.ShoppingCart;
import com.smartshop.entity.product.Product;
import com.smartshop.entity.product.ProductVariant;
import com.smartshop.repository.CartItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class WishlistService {

    private final CartService cartService;
    private final ProductService productService;
    private final CartItemRepository cartItemRepository;

    public List<CartItem> getWishlistItems(Long userId) {
        ShoppingCart cart = cartService.getCartByUserId(userId);
        return cartItemRepository.findByCartIdAndWishlistTrue(cart.getId());
    }

    public void addToWishlist(Long userId, Long productId, Long variantId) {
        Objects.requireNonNull(productId, "productId must not be null");

        ShoppingCart cart = cartService.getCartByUserId(userId);
        Product product = productService.getProductOrThrow(productId);
        ProductVariant variant = productService.getVariant(productId, variantId).orElse(null);

        if (product.isHasVariants() && variant == null) {
            throw new IllegalArgumentException("Vui lòng chọn phiên bản sản phẩm");
        }

        boolean alreadyExists = cart.getItems().stream()
                .anyMatch(item -> item.isWishlist()
                        && item.getProduct().getId().equals(productId)
                        && ((variant == null && item.getVariant() == null)
                        || (variant != null && item.getVariant() != null
                        && item.getVariant().getId().equals(variant.getId()))));

        if (alreadyExists) {
            return;
        }

        CartItem wishlistItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .variant(variant)
                .quantity(1)
                .wishlist(true)
                .build();

        cart.getItems().add(wishlistItem);
        cartItemRepository.save(wishlistItem);
    }

    public void removeFromWishlist(Long userId, Long itemId) {
        ShoppingCart cart = cartService.getCartByUserId(userId);
        CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.isWishlist() && cartItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Wishlist item not found"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
    }
}

