package com.smartshop.service;

import com.smartshop.entity.product.Category;
import com.smartshop.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Category getCategory(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Danh mục không tồn tại"));
    }

    public Category createCategory(String name, Long parentId) {
        Objects.requireNonNull(name, "name must not be null");
        validateUniqueName(name, null);

        Category category = new Category();
        category.setName(name.trim());

        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Danh mục cha không tồn tại"));
            category.setParent(parent);
        }

        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, String name, Long parentId) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");

        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Danh mục không tồn tại"));

        validateUniqueName(name, id);

        existing.setName(name.trim());

        if (parentId != null) {
            if (parentId.equals(id)) {
                throw new IllegalArgumentException("Danh mục cha không được trùng với chính nó");
            }
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Danh mục cha không tồn tại"));
            existing.setParent(parent);
        } else {
            existing.setParent(null);
        }

        return categoryRepository.save(existing);
    }

    public void deleteCategory(Long id) {
        Objects.requireNonNull(id, "id must not be null");

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Danh mục không tồn tại"));

        if (!category.getProducts().isEmpty()) {
            throw new DataIntegrityViolationException("Không thể xóa danh mục đang chứa sản phẩm");
        }

        categoryRepository.delete(category);
    }

    private void validateUniqueName(String name, Long excludeId) {
        Optional<Category> existing = categoryRepository.findByName(name.trim());
        if (existing.isPresent() && (excludeId == null || !existing.get().getId().equals(excludeId))) {
            throw new DataIntegrityViolationException("Tên danh mục đã tồn tại");
        }
    }
}

