package com.smartshop.controller.api;

import com.smartshop.dto.request.product.CreateProductRequest;
import com.smartshop.dto.request.product.SearchProductRequest;
import com.smartshop.dto.request.product.UpdateProductRequest;
import com.smartshop.dto.response.ApiResponse;
import com.smartshop.dto.response.product.ProductDetailResponse;
import com.smartshop.dto.response.product.ProductResponse;
import com.smartshop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý các API liên quan đến sản phẩm
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    /**
     * Tạo mới sản phẩm
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductDetailResponse createdProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdProduct, "Tạo sản phẩm thành công"));
    }

    /**
     * Cập nhật thông tin sản phẩm
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductDetailResponse updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedProduct, "Cập nhật sản phẩm thành công"));
    }

    /**
     * Lấy thông tin chi tiết sản phẩm theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(@PathVariable Long id) {
        ProductDetailResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    /**
     * Tìm kiếm sản phẩm với các bộ lọc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(@Valid SearchProductRequest request) {
        Page<ProductResponse> result = productService.searchProducts(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Lấy thông tin chi tiết sản phẩm theo slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductBySlug(@PathVariable String slug) {
        ProductDetailResponse product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    /**
     * Xóa sản phẩm
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa sản phẩm thành công"));
    }

    /**
     * Lấy danh sách sản phẩm mới nhất
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLatestProducts(
            @RequestParam(defaultValue = "8") int limit) {
        List<ProductResponse> products = productService.getLatestProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * Lấy danh sách sản phẩm bán chạy
     */
    @GetMapping("/best-sellers")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getBestSellingProducts(
            @RequestParam(defaultValue = "8") int limit) {
        List<ProductResponse> products = productService.getBestSellingProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * Lấy danh sách sản phẩm khuyến mãi
     */
    @GetMapping("/on-sale")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsOnSale(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<ProductResponse> products = productService.getProductsOnSale(page, size);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * Lấy danh sách sản phẩm theo danh mục
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<ProductResponse> products = productService.getProductsByCategory(categoryId, page, size);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * Lấy danh sách danh mục đang hoạt động
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(productService.getActiveCategories()));
    }
}
