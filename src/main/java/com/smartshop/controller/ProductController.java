package com.smartshop.controller;

import com.smartshop.dto.product.ProductSummaryView;
import com.smartshop.entity.product.Product;
import com.smartshop.entity.product.ProductVariant;
import com.smartshop.entity.product.ProductImage;
import com.smartshop.service.CategoryService;
import com.smartshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @Value("${CLOUD_NAME:Root}")
    private String cloudName;

    // ============================================================
    // GET - Danh sách sản phẩm với tìm kiếm và lọc
    // ============================================================

    @GetMapping
    public String listProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String direction,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            Model model) {

        // Tạo Pageable với sắp xếp
        Pageable pageable = createPageable(page, size, sortBy, direction);

        // Tìm kiếm sản phẩm
        Page<Product> productPage = productService.searchProducts(
                keyword, categoryId, minPrice, maxPrice, pageable);

        // Chuyển đổi Product thành ProductSummaryView
        List<ProductSummaryView> productSummaries = convertToProductSummaries(productPage.getContent());

        // Thêm dữ liệu vào model
        addProductListAttributes(model, productPage, productSummaries, keyword, 
                categoryId, minPrice, maxPrice, sortBy, direction);

        return "product/list";
    }

    // ============================================================
    // GET - Chi tiết sản phẩm
    // ============================================================

    @GetMapping("/{identifier}")
    public String viewProduct(@PathVariable String identifier, Model model) {
        // Lấy sản phẩm theo ID hoặc slug
        Product product = getProductByIdentifier(identifier);

        // Lấy danh sách variants
        List<ProductVariant> variants = productService.getActiveVariants(product.getId());

        // Lấy gallery ảnh
        List<String> gallery = buildProductGallery(product);

        // Lấy ảnh chính
        String primaryImageUrl = getPrimaryImageUrl(product.getId());

        // Thêm dữ liệu vào model
        addProductDetailAttributes(model, product, variants, gallery, primaryImageUrl);

        return "product/detail";
    }

    // ============================================================
    // PRIVATE HELPER METHODS - Tạo Pageable
    // ============================================================

    /**
     * Tạo Pageable object với sắp xếp
     */
    private Pageable createPageable(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        Sort sort = Sort.by(sortDirection, sortBy);
        
        // Đảm bảo page và size hợp lệ
        int validPage = Math.max(page, 0);
        int validSize = Math.max(size, 1);
        
        return PageRequest.of(validPage, validSize, sort);
    }

    // ============================================================
    // PRIVATE HELPER METHODS - Chuyển đổi dữ liệu
    // ============================================================

    /**
     * Chuyển đổi danh sách Product thành ProductSummaryView
     */
    private List<ProductSummaryView> convertToProductSummaries(List<Product> products) {
        return products.stream()
                .map(this::convertToProductSummary)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi một Product thành ProductSummaryView
     */
    private ProductSummaryView convertToProductSummary(Product product) {
        String imageUrl = getPrimaryImageUrl(product.getId());
        
        return ProductSummaryView.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .hasVariants(product.isHasVariants())
                .imageUrl(imageUrl)
                .build();
    }

    // ============================================================
    // PRIVATE HELPER METHODS - Lấy sản phẩm
    // ============================================================

    /**
     * Lấy sản phẩm theo ID (số) hoặc slug (chuỗi)
     */
    private Product getProductByIdentifier(String identifier) {
        try {
            // Thử parse thành Long (ID)
            Long id = Long.parseLong(identifier);
            return productService.getProductDetail(id);
        } catch (NumberFormatException ex) {
            // Nếu không phải số, coi như slug
            return productService.getProductDetail(identifier);
        }
    }

    // ============================================================
    // PRIVATE HELPER METHODS - Xử lý ảnh
    // ============================================================

    /**
     * Lấy URL ảnh chính của sản phẩm
     */
    private String getPrimaryImageUrl(Long productId) {
        return productService.getPrimaryImage(productId)
                .map(ProductImage::getPublicId)
                .map(this::buildCloudinaryUrl)
                .orElse(null);
    }

    /**
     * Tạo danh sách gallery ảnh từ sản phẩm
     */
    private List<String> buildProductGallery(Product product) {
        return product.getImages().stream()
                .map(ProductImage::getPublicId)
                .map(this::buildCloudinaryUrl)
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.toList());
    }

    /**
     * Xây dựng URL ảnh Cloudinary từ publicId
     */
    private String buildCloudinaryUrl(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return null;
        }
        
        // Nếu đã là URL đầy đủ, trả về luôn
        if (publicId.startsWith("http")) {
            return publicId;
        }
        
        // Xây dựng URL Cloudinary
        String cloudNameValue = (cloudName != null && !cloudName.isBlank()) ? cloudName : "Root";
        return "https://res.cloudinary.com/" + cloudNameValue + "/image/upload/" + publicId;
    }

    // ============================================================
    // PRIVATE HELPER METHODS - Thêm attributes vào Model
    // ============================================================

    /**
     * Thêm các attributes cho trang danh sách sản phẩm
     */
    private void addProductListAttributes(Model model, Page<Product> productPage,
                                         List<ProductSummaryView> products, String keyword,
                                         Long categoryId, Double minPrice, Double maxPrice,
                                         String sort, String direction) {
        model.addAttribute("page", productPage);
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("categories", categoryService.getAllCategories());
    }

    /**
     * Thêm các attributes cho trang chi tiết sản phẩm
     */
    private void addProductDetailAttributes(Model model, Product product,
                                           List<ProductVariant> variants,
                                           List<String> gallery, String primaryImageUrl) {
        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("primaryImage", primaryImageUrl);
        model.addAttribute("gallery", gallery);
    }
}
