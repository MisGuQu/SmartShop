package com.smartshop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; 
import com.smartshop.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNullAndIsActiveTrue();
    List<Category> findByIsActiveTrue();
    Optional<Category> findByName(String name);
}
