package com.smartshop.service;

import com.smartshop.dto.cart.CartSummaryView;
import com.smartshop.dto.order.CheckoutRequest;
import com.smartshop.entity.cart.CartItem;
import com.smartshop.entity.cart.ShoppingCart;
import com.smartshop.entity.enums.PaymentStatus;
import com.smartshop.entity.order.Order;
import com.smartshop.entity.order.OrderItem;
import com.smartshop.entity.product.Product;
import com.smartshop.entity.product.ProductVariant;
import com.smartshop.entity.user.User;
import com.smartshop.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class CheckoutService {

    private final CartService cartService;
    private final OrderService orderService;
    private final ProductService productService;
    private final UserRepository userRepository;

    @Value("${CLOUD_NAME:Root}")
    private String cloudName;

    public Order placeOrder(Long userId, CheckoutRequest request) {
        Objects.requireNonNull(userId, "userId must not be null");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Người dùng không tồn tại"));

        ShoppingCart cart = cartService.getCartByUserId(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống");
        }

        CartSummaryView summary = cartService.getCartSummary(userId);
        double subtotal = summary.getSubtotal();
        double shippingFee = request.getShippingMethod().getFee();

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            ProductVariant variant = cartItem.getVariant();
            double unitPrice = variant != null && variant.getPrice() != null
                    ? variant.getPrice()
                    : product.getBasePrice();

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .variant(variant)
                    .productName(product.getName())
                    .variantDetails(buildVariantLabel(variant))
                    .productImagePublicId(productService.getPrimaryImage(product.getId())
                            .map(img -> buildCloudinaryUrl(img.getPublicId()))
                            .orElse(null))
                    .quantity(cartItem.getQuantity())
                    .pricePerUnit(unitPrice)
                    .subtotal(unitPrice * cartItem.getQuantity())
                    .build();
            orderItems.add(orderItem);

            if (variant != null) {
                productService.reduceInventory(variant, cartItem.getQuantity());
            }
        }

        Order order = orderService.createOrderFromCart(user, request, subtotal, shippingFee, orderItems);
        order.setPaymentStatus(PaymentStatus.PENDING);

        cartService.clearCart(userId);
        return order;
    }

    private String buildVariantLabel(ProductVariant variant) {
        if (variant == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        if (variant.getColor() != null && !variant.getColor().isBlank()) {
            builder.append(variant.getColor());
        }
        if (variant.getSize() != null && !variant.getSize().isBlank()) {
            if (builder.length() > 0) {
                builder.append(" | ");
            }
            builder.append(variant.getSize());
        }
        if (variant.getStorage() != null && !variant.getStorage().isBlank()) {
            if (builder.length() > 0) {
                builder.append(" | ");
            }
            builder.append(variant.getStorage());
        }
        return builder.length() > 0 ? builder.toString() : null;
    }

    private String buildCloudinaryUrl(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return null;
        }
        if (publicId.startsWith("http")) {
            return publicId;
        }
        String name = (cloudName != null && !cloudName.isBlank()) ? cloudName : "Root";
        return "https://res.cloudinary.com/" + name + "/image/upload/" + publicId;
    }
}

