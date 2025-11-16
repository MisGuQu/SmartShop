package com.smartshop.dto.product;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryForm {

    private Long id;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private Long parentId;
}

