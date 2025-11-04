package com.smartshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartshop.entity.product.VariantImage;

public interface VariantImageRepository extends JpaRepository<VariantImage, Long> {
    List<VariantImage> findByVariantIdOrderByDisplayOrderAsc(Long variantId);
    Optional<VariantImage> findByVariantIdAndIsPrimaryTrue(Long variantId);
}
