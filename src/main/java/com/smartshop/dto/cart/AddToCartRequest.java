package com.smartshop.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequest {

    @NotNull
    private Long productId;

    private Long variantId;

    @Min(1)
    private int quantity = 1;
}

