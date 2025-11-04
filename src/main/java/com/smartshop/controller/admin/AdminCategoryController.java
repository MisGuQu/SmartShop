package com.smartshop.controller.admin;

import com.smartshop.dto.CategoryDTO;
import com.smartshop.dto.request.CreateCategoryRequest;
import com.smartshop.dto.request.UpdateCategoryRequest;
import com.smartshop.dto.response.ApiResponse;
import com.smartshop.service.admin.AdminCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý danh mục sản phẩm (Admin)
 */
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final AdminCategoryService categoryService;

    /**
     * Tạo mới danh mục
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryDTO category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(category, "Tạo danh mục thành công"));
    }

    /**
     * Lấy tất cả danh mục (có thể dùng cho cây danh mục)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * Cập nhật danh mục
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryDTO category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success(category, "Cập nhật danh mục thành công"));
    }

    /**
     * Xóa danh mục (soft delete hoặc kiểm tra sản phẩm)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa danh mục thành công"));
    }

    /**
     * Lấy danh mục cha (dùng cho tree select)
     */
    @GetMapping("/parents")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getParentCategories() {
        List<CategoryDTO> parents = categoryService.getParentCategories();
        return ResponseEntity.ok(ApiResponse.success(parents));
    }
}