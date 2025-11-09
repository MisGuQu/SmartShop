package com.smartshop.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductForm {

    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String slug;

    private String description;

    @NotNull(message = "Giá cơ bản không được để trống")
    @Positive(message = "Giá cơ bản phải lớn hơn 0")
    private Double basePrice;

    private Long categoryId;

    private boolean hasVariants = false;

    private Integer stockQuantity = 0;

    private boolean active = true;

    private String brand;

    private Double weight;

    private String metaTitle;

    private String metaDescription;
}

