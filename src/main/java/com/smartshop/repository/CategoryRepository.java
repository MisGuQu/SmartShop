package com.smartshop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartshop.entity.product.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNullAndIsActiveTrue();
    List<Category> findByIsActiveTrue();
    Optional<Category> findByName(String name);
}
