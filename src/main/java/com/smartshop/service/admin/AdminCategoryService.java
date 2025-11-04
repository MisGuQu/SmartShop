package com.smartshop.service.admin;

import com.smartshop.dto.CategoryDTO;
import com.smartshop.dto.request.CreateCategoryRequest;
import com.smartshop.dto.request.UpdateCategoryRequest;

import java.util.List;

/**
 * Service interface for admin category operations
 */
public interface AdminCategoryService {
    
    /**
     * Tạo mới danh mục
     */
    CategoryDTO createCategory(CreateCategoryRequest request);
    
    /**
     * Lấy tất cả danh mục
     */
    List<CategoryDTO> getAllCategories();
    
    /**
     * Cập nhật danh mục
     */
    CategoryDTO updateCategory(Long id, UpdateCategoryRequest request);
    
    /**
     * Xóa danh mục
     */
    void deleteCategory(Long id);
    
    /**
     * Lấy danh sách danh mục cha
     */
    List<CategoryDTO> getParentCategories();
}
