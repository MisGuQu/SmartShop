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

    @GetMapping
    public String listProducts(@RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "12") int size,
                               @RequestParam(value = "sort", defaultValue = "createdAt") String sortBy,
                               @RequestParam(value = "direction", defaultValue = "desc") String direction,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "category", required = false) Long categoryId,
                               @RequestParam(value = "minPrice", required = false) Double minPrice,
                               @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                               Model model) {

        Sort sort = Sort.by("asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sort);

        Page<Product> productPage = productService.searchProducts(keyword, categoryId, minPrice, maxPrice, pageable);

        List<ProductSummaryView> summaries = productPage.getContent().stream()
                .map(product -> ProductSummaryView.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .slug(product.getSlug())
                        .description(product.getDescription())
                        .price(product.getBasePrice())
                        .hasVariants(product.isHasVariants())
                        .imageUrl(productService.getPrimaryImage(product.getId())
                                .map(ProductImage::getPublicId)
                                .map(this::buildCloudinaryUrl)
                                .orElse(null))
                        .build())
                .collect(Collectors.toList());

        model.addAttribute("page", productPage);
        model.addAttribute("products", summaries);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "product/list";
    }

    @GetMapping("/{identifier}")
    public String viewProduct(@PathVariable String identifier, Model model) {
        Product product;
        try {
            Long id = Long.parseLong(identifier);
            product = productService.getProductDetail(id);
        } catch (NumberFormatException ex) {
            product = productService.getProductDetail(identifier);
        }

        List<ProductVariant> variants = productService.getActiveVariants(product.getId());
        List<String> gallery = product.getImages().stream()
                .map(ProductImage::getPublicId)
                .map(this::buildCloudinaryUrl)
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.toList());

        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("primaryImage", productService.getPrimaryImage(product.getId())
                .map(ProductImage::getPublicId)
                .map(this::buildCloudinaryUrl)
                .orElse(null));
        model.addAttribute("gallery", gallery);

        return "product/detail";
    }

    private String buildCloudinaryUrl(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return null;
        }
        if (publicId.startsWith("http")) {
            return publicId;
        }
        String name = (cloudName != null && !cloudName.isBlank()) ? cloudName : "Root";
        return "https://res.cloudinary.com/" + name + "/image/upload/" + publicId;
    }
}
