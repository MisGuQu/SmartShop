package com.smartshop.service;

import com.smartshop.dto.request.product.CreateProductRequest;
import com.smartshop.dto.request.product.SearchProductRequest;
import com.smartshop.dto.request.product.UpdateProductRequest;
import com.smartshop.dto.response.product.ProductDetailResponse;
import com.smartshop.dto.response.product.ProductResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for product operations
 */
public interface ProductService {

    /**
     * Tạo mới sản phẩm
     */
    ProductDetailResponse createProduct(CreateProductRequest request);

    /**
     * Cập nhật thông tin sản phẩm
     */
    ProductDetailResponse updateProduct(Long id, UpdateProductRequest request);

    /**
     * Lấy thông tin chi tiết sản phẩm theo ID
     */
    ProductDetailResponse getProductById(Long id);

    /**
     * Tìm kiếm sản phẩm với các bộ lọc
     */
    Page<ProductResponse> searchProducts(SearchProductRequest request);

    /**
     * Lấy thông tin chi tiết sản phẩm theo slug
     */
    ProductDetailResponse getProductBySlug(String slug);

    /**
     * Xóa sản phẩm
     */
    void deleteProduct(Long id);

    /**
     * Lấy danh sách sản phẩm mới nhất
     */
    List<ProductResponse> getLatestProducts(int limit);

    /**
     * Lấy danh sách sản phẩm bán chạy
     */
    List<ProductResponse> getBestSellingProducts(int limit);

    /**
     * Lấy danh sách sản phẩm đang giảm giá
     */
    Page<ProductResponse> getProductsOnSale(int page, int size);

    /**
     * Lấy danh sách sản phẩm theo danh mục
     */
    Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size);
}
