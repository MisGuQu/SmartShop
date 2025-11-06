package com.smartshop.service;

import com.smartshop.entity.cart.Wishlist;
import com.smartshop.entity.cart.WishlistItem;
import com.smartshop.entity.product.Product;
import com.smartshop.entity.product.ProductVariant;
import com.smartshop.entity.user.User;
import com.smartshop.repository.ProductRepository;
import com.smartshop.repository.ProductVariantRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.repository.WishlistItemRepository;
import com.smartshop.repository.WishlistRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@SuppressWarnings({"DataFlowIssue", "NullAway"})
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public Wishlist getWishlistByUser(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return loadOrCreateWishlist(userId);
    }

    @SuppressWarnings("DataFlowIssue")
    public Wishlist addToWishlist(Long userId, Long productId, Long variantId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(productId, "productId must not be null");

        Wishlist wishlist = loadOrCreateWishlist(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        ProductVariant variant = null;
        if (variantId != null) {
            variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new EntityNotFoundException("Product variant not found"));
        }

        boolean exists = variant != null
                ? wishlistItemRepository.existsByWishlistIdAndProductIdAndVariantId(wishlist.getId(), productId, variant.getId())
                : wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId);

        if (!exists) {
            WishlistItem item = WishlistItem.builder()
                    .wishlist(wishlist)
                    .product(product)
                    .variant(variant)
                    .build();
            wishlist.getItems().add(item);
            wishlistRepository.save(wishlist);
        }

        return Objects.requireNonNull(loadOrCreateWishlist(userId));
    }

    public Wishlist removeFromWishlist(Long userId, Long itemId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(itemId, "itemId must not be null");

        Wishlist wishlist = loadOrCreateWishlist(userId);

        WishlistItem item = wishlistItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Wishlist item not found"));

        Wishlist owner = item.getWishlist();
        if (owner == null) {
            throw new IllegalStateException("Permission denied");
        }
        Long ownerId = owner.getId();
        if (ownerId == null || !ownerId.equals(wishlist.getId())) {
            throw new IllegalStateException("Permission denied");
        }

        if (owner.getItems() != null) {
            owner.getItems().remove(item);
        }
        wishlistItemRepository.delete(item);
        wishlistRepository.save(owner);
        Wishlist refreshed = loadOrCreateWishlist(userId);
        return Objects.requireNonNull(refreshed);
    }

    @SuppressWarnings("NullAway")
    private Wishlist loadOrCreateWishlist(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Wishlist existing = wishlistRepository.findByUserId(userId).orElse(null);
        if (existing != null) {
            return existing;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .build();
        wishlistRepository.save(wishlist);
        return Objects.requireNonNull(wishlist);
    }
}
