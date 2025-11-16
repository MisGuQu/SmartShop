package com.smartshop.service;

import com.smartshop.dto.cart.CartItemView;
import com.smartshop.dto.cart.CartSummaryView;
import com.smartshop.entity.cart.CartItem;
import com.smartshop.entity.cart.ShoppingCart;
import com.smartshop.entity.product.Product;
import com.smartshop.entity.product.ProductImage;
import com.smartshop.entity.product.ProductVariant;
import com.smartshop.entity.user.User;
import com.smartshop.repository.CartItemRepository;
import com.smartshop.repository.ShoppingCartRepository;
import com.smartshop.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    @Value("${CLOUD_NAME:Root}")
    private String cloudName;

    @SuppressWarnings("null")
    public ShoppingCart getCartByUserId(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Optional<ShoppingCart> existing = shoppingCartRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        ShoppingCart cart = ShoppingCart.builder()
                .user(resolveUser(userId))
                .build();
        return shoppingCartRepository.save(cart);
    }

    public CartSummaryView getCartSummary(Long userId) {
        ShoppingCart cart = getCartByUserId(userId);
        List<CartItemView> items = cart.getItems().stream()
                .filter(item -> !item.isWishlist())
                .sorted(Comparator.comparing(CartItem::getId))
                .map(this::toCartItemView)
                .collect(Collectors.toList());

        double subtotal = items.stream().mapToDouble(CartItemView::getSubtotal).sum();
        int totalQuantity = items.stream().mapToInt(CartItemView::getQuantity).sum();

        return CartSummaryView.builder()
                .items(items)
                .subtotal(round(subtotal))
                .totalQuantity(totalQuantity)
                .empty(items.isEmpty())
                .build();
    }

    public ShoppingCart addItem(Long userId, Long productId, Long variantId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        ShoppingCart cart = getCartByUserId(userId);
        Product product = productService.getProductOrThrow(productId);
        ProductVariant variant = productService.getVariant(productId, variantId).orElse(null);

        if (product.isHasVariants() && variant == null) {
            throw new IllegalArgumentException("Vui lòng chọn phiên bản sản phẩm");
        }

        CartItem existing = cartItemRepository.findByCartProductAndVariant(cart.getId(), productId, variantId)
                .orElse(null);

        int desiredQuantity = quantity + (existing != null ? existing.getQuantity() : 0);
        validateInventory(product, variant, desiredQuantity);

        if (existing != null) {
            existing.setQuantity(desiredQuantity);
            return shoppingCartRepository.save(cart);
        }

        CartItem newItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .variant(variant)
                .quantity(quantity)
                .build();

        cart.getItems().add(newItem);
        return shoppingCartRepository.save(cart);
    }

    public ShoppingCart updateItem(Long userId, Long cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        ShoppingCart cart = getCartByUserId(userId);
        CartItem item = cart.getItems().stream()
                .filter(ci -> !ci.isWishlist() && ci.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm trong giỏ không tồn tại"));

        Product product = item.getProduct();
        ProductVariant variant = item.getVariant();
        validateInventory(product, variant, quantity);

        item.setQuantity(quantity);
        return shoppingCartRepository.save(cart);
    }

    public ShoppingCart removeItem(Long userId, Long cartItemId) {
        ShoppingCart cart = getCartByUserId(userId);
        CartItem item = cart.getItems().stream()
                .filter(ci -> !ci.isWishlist() && ci.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm trong giỏ không tồn tại"));

        Objects.requireNonNull(item, "cartItem must not be null");
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return shoppingCartRepository.save(cart);
    }

    public void clearCart(Long userId) {
        ShoppingCart cart = getCartByUserId(userId);
        List<CartItem> existingItems = cart.getItems().stream()
                .filter(item -> !item.isWishlist())
                .collect(Collectors.toList());
        existingItems.forEach(cartItemRepository::delete);
        cart.getItems().clear();
        shoppingCartRepository.save(cart);
    }

    private CartItemView toCartItemView(CartItem item) {
        Product product = item.getProduct();
        ProductVariant variant = item.getVariant();
        double unitPrice = variant != null && variant.getPrice() != null
                ? variant.getPrice()
                : product.getPrice();
        double subtotal = unitPrice * item.getQuantity();
        Optional<ProductImage> primaryImage = productService.getPrimaryImage(product.getId());
        String imageUrl = primaryImage
                .map(ProductImage::getPublicId)
                .map(this::buildCloudinaryUrl)
                .orElse(null);

        return CartItemView.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSlug(product.getSlug())
                .variantId(variant != null ? variant.getId() : null)
                .variantLabel(buildVariantLabel(variant))
                .unitPrice(round(unitPrice))
                .quantity(item.getQuantity())
                .subtotal(round(subtotal))
                .maxQuantity(variant != null ? variant.getStock() : 99)
                .imageUrl(imageUrl)
                .build();
    }

    private void validateInventory(Product product, ProductVariant variant, int desiredQuantity) {
        if (variant != null) {
            if (variant.getStock() < desiredQuantity) {
                throw new IllegalStateException("Sản phẩm không đủ tồn kho");
            }
        }
    }

    private String buildVariantLabel(ProductVariant variant) {
        if (variant == null) {
            return null;
        }
        return StringUtils.hasText(variant.getVariantName()) ? variant.getVariantName() : null;
    }

    private User resolveUser(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Người dùng không tồn tại"));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String buildCloudinaryUrl(String publicId) {
        if (!StringUtils.hasText(publicId)) {
            return null;
        }
        if (publicId.startsWith("http")) {
            return publicId;
        }
        String name = StringUtils.hasText(cloudName) ? cloudName : "Root";
        return "https://res.cloudinary.com/" + name + "/image/upload/" + publicId;
    }
}
