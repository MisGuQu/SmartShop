package com.smartshop.dto.cart;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CartItemView {
    Long id;
    Long productId;
    String productName;
    String productSlug;
    Long variantId;
    String variantLabel;
    String imageUrl;
    double unitPrice;
    int quantity;
    double subtotal;
    int maxQuantity;
}

