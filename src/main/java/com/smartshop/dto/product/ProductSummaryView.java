package com.smartshop.dto.product;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductSummaryView {
    Long id;
    String name;
    String slug;
    String description;
    Double price;
    boolean hasVariants;
    String imageUrl;
}

