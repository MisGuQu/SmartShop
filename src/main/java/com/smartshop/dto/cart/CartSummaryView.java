package com.smartshop.dto.cart;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CartSummaryView {
    @Singular
    List<CartItemView> items;
    double subtotal;
    int totalQuantity;
    boolean empty;
}

