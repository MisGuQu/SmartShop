package com.smartshop.repository.spec;

import com.smartshop.entity.product.Product;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> isActive() {
        return (root, query, builder) -> builder.isTrue(root.get("isActive"));
    }

    public static Specification<Product> keyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String likePattern = "%" + keyword.trim().toLowerCase().replace(" ", "%") + "%";
        return (root, query, builder) -> builder.like(builder.lower(root.get("name")), likePattern);
    }

    public static Specification<Product> category(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.join("category", JoinType.LEFT).get("id"), categoryId);
    }

    public static Specification<Product> minPrice(Double minPrice) {
        if (minPrice == null) {
            return null;
        }
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> maxPrice(Double maxPrice) {
        if (maxPrice == null) {
            return null;
        }
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

}

