package com.smartshop.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để cập nhật thông tin danh mục
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String name;
    
    private String description;
    
    private String imageUrl;
    
    private Long parentId;
    
    private Integer displayOrder;
    
    private Boolean active;
    
    @jakarta.persistence.Version
    private Long version;
}
