package com.smartshop.service;

import com.smartshop.entity.product.Product; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    public List<Product> getAllProducts() {
        // TODO: Lấy danh sách sản phẩm
        return List.of();
    }

    public Optional<Product> getProductById(Long id) {
        // TODO: Lấy chi tiết sản phẩm
        return Optional.empty();
    }

    public Product createProduct(Product product) {
        // TODO: Thêm sản phẩm
        return product;
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        // TODO: Cập nhật sản phẩm
        return updatedProduct;
    }

    public void deleteProduct(Long id) {
        // TODO: Xóa sản phẩm
    }

    public List<Product> searchProducts(String keyword) {
        // TODO: Tìm kiếm sản phẩm theo tên hoặc danh mục
        return List.of();
    }
}
