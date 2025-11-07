package com.smartshop.service;

import com.smartshop.entity.product.Category;
import com.smartshop.entity.product.Product;
import com.smartshop.entity.product.ProductImage;
import com.smartshop.entity.product.ProductVariant;
import com.smartshop.repository.CategoryRepository;
import com.smartshop.repository.ProductImageRepository;
import com.smartshop.repository.ProductRepository;
import com.smartshop.repository.ProductVariantRepository;
import com.smartshop.repository.spec.ProductSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Optional<Product> getProductById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        return productRepository.findById(id);
    }

    public Product getProductOrThrow(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại"));
    }

    public Product createProduct(Product product, Long categoryId) {
        Objects.requireNonNull(product, "product must not be null");

        String name = normalizeName(product.getName());
        product.setName(name);

        if (product.getBasePrice() == null || product.getBasePrice() <= 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0");
        }

        Category category = resolveCategory(categoryId);

        product.setId(null);
        product.setCategory(category);
        product.setSlug(generateUniqueSlug(name, null));

        if (!StringUtils.hasText(product.getDescription())) {
            product.setDescription(null);
        }

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updatedProduct, Long categoryId) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(updatedProduct, "updatedProduct must not be null");

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại"));

        String newName = normalizeName(updatedProduct.getName());
        if (!existing.getName().equals(newName)) {
            existing.setSlug(generateUniqueSlug(newName, existing.getId()));
        }
        existing.setName(newName);

        if (updatedProduct.getBasePrice() == null || updatedProduct.getBasePrice() <= 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0");
        }
        existing.setBasePrice(updatedProduct.getBasePrice());

        existing.setDescription(StringUtils.hasText(updatedProduct.getDescription())
                ? updatedProduct.getDescription()
                : null);

        existing.setActive(updatedProduct.isActive());

        Category category = resolveCategory(categoryId);
        existing.setCategory(category);

        return productRepository.save(existing);
    }

    public void deleteProduct(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại"));

        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            throw new DataIntegrityViolationException("Không thể xóa sản phẩm đang có phiên bản");
        }

        productRepository.delete(product);
    }

    public List<Product> searchProducts(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getAllProducts();
        }
        return productRepository.search(keyword.trim(), Pageable.unpaged()).getContent();
    }

    public Page<Product> searchProducts(String keyword,
                                        Long categoryId,
                                        Double minPrice,
                                        Double maxPrice,
                                        Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable must not be null");
        Specification<Product> spec = Specification.where(ProductSpecifications.isActive());

        Specification<Product> keywordSpec = ProductSpecifications.keyword(keyword);
        if (keywordSpec != null) {
            spec = spec.and(keywordSpec);
        }

        Specification<Product> categorySpec = ProductSpecifications.category(categoryId);
        if (categorySpec != null) {
            spec = spec.and(categorySpec);
        }

        Specification<Product> minPriceSpec = ProductSpecifications.minPrice(minPrice);
        if (minPriceSpec != null) {
            spec = spec.and(minPriceSpec);
        }

        Specification<Product> maxPriceSpec = ProductSpecifications.maxPrice(maxPrice);
        if (maxPriceSpec != null) {
            spec = spec.and(maxPriceSpec);
        }

        return productRepository.findAll(spec, pageable);
    }

    public Product getProductDetail(Long productId) {
        Product product = getProductOrThrow(productId);
        Hibernate.initialize(product.getImages());
        Hibernate.initialize(product.getVariants());
        product.getImages().sort(Comparator.comparing(ProductImage::isPrimary).reversed()
                .thenComparing(ProductImage::getDisplayOrder));
        product.getVariants().sort(Comparator.comparing(ProductVariant::getId));
        return product;
    }

    public Product getProductDetail(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new EntityNotFoundException("Slug không hợp lệ");
        }
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại"));
        Hibernate.initialize(product.getImages());
        Hibernate.initialize(product.getVariants());
        product.getImages().sort(Comparator.comparing(ProductImage::isPrimary).reversed()
                .thenComparing(ProductImage::getDisplayOrder));
        product.getVariants().sort(Comparator.comparing(ProductVariant::getId));
        return product;
    }

    public List<ProductVariant> getActiveVariants(Long productId) {
        return productVariantRepository.findByProductIdAndIsActiveTrue(productId);
    }

    public Optional<ProductVariant> getVariant(Long productId, Long variantId) {
        if (variantId == null) {
            return Optional.empty();
        }
        return productVariantRepository.findById(variantId)
                .filter(variant -> variant.getProduct() != null && variant.getProduct().getId().equals(productId));
    }

    public Optional<ProductImage> getPrimaryImage(Product product) {
        if (product == null) {
            return Optional.empty();
        }
        if (Hibernate.isInitialized(product.getImages()) && !product.getImages().isEmpty()) {
            return product.getImages().stream()
                    .sorted(Comparator.comparing(ProductImage::isPrimary).reversed()
                            .thenComparing(ProductImage::getDisplayOrder))
                    .findFirst();
        }
        return getPrimaryImage(product.getId());
    }

    public Optional<ProductImage> getPrimaryImage(Long productId) {
        if (productId == null) {
            return Optional.empty();
        }
        return productImageRepository.findByProductIdAndIsPrimaryTrue(productId)
                .or(() -> {
                    List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
                    return images.stream().findFirst();
                });
    }

    @Transactional
    public void reduceInventory(ProductVariant variant, int quantity) {
        if (variant == null || quantity <= 0) {
            return;
        }
        if (variant.getStockQuantity() < quantity) {
            throw new IllegalStateException("Sản phẩm không đủ tồn kho");
        }
        variant.setStockQuantity(variant.getStockQuantity() - quantity);
        productVariantRepository.save(variant);
    }

    @Transactional
    public void increaseInventory(ProductVariant variant, int quantity) {
        if (variant == null || quantity <= 0) {
            return;
        }
        variant.setStockQuantity(variant.getStockQuantity() + quantity);
        productVariantRepository.save(variant);
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Danh mục không tồn tại"));
    }

    private String normalizeName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        }
        return name.trim();
    }

    private String generateUniqueSlug(String source, Long excludeId) {
        String baseSlug = slugify(source);
        String candidate = baseSlug;
        int counter = 1;

        while (true) {
            Optional<Product> existing = productRepository.findBySlug(candidate);
            if (existing.isEmpty() || (excludeId != null && existing.get().getId().equals(excludeId))) {
                return candidate;
            }
            candidate = baseSlug + "-" + counter++;
        }
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug = normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (!StringUtils.hasText(slug)) {
            slug = "san-pham";
        }
        return slug;
    }
}
