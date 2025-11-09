package com.smartshop.controller;

import com.smartshop.dto.product.CategoryForm;
import com.smartshop.dto.product.ProductForm;
import com.smartshop.entity.product.Category;
import com.smartshop.entity.product.Product;
import com.smartshop.service.CategoryService;
import com.smartshop.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @ModelAttribute("categories")
    public List<Category> categories() {
        return categoryService.getAllCategories();
    }

    @GetMapping
    public String showProductManagement(@RequestParam(value = "editId", required = false) Long editId,
                                        Model model) {
        model.addAttribute("products", productService.getAllProducts());

        if (!model.containsAttribute("productForm")) {
            if (editId != null) {
                Product product = productService.getProductOrThrow(editId);
                model.addAttribute("productForm", mapToForm(product));
                model.addAttribute("editingProductId", editId);
            } else {
                model.addAttribute("productForm", new ProductForm());
            }
        } else if (editId != null) {
            model.addAttribute("editingProductId", editId);
        }

        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new CategoryForm());
        }

        return "admin/products";
    }

    @PostMapping
    public String createProduct(@Valid @ModelAttribute("productForm") ProductForm productForm,
                                BindingResult bindingResult,
                                @RequestParam(value = "images", required = false) MultipartFile[] images,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(model, null);
        }

        try {
            Product product = productService.createProduct(mapToEntity(productForm), productForm.getCategoryId());
            // Upload hình ảnh nếu có
            if (images != null && images.length > 0) {
                productService.uploadProductImages(product.getId(), images);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công");
            return "redirect:/admin/products";
        } catch (EntityNotFoundException | IllegalArgumentException ex) {
            bindingResult.reject("product.error", safeMessage(ex.getMessage(), "Không thể lưu sản phẩm"));
        } catch (DataIntegrityViolationException ex) {
            String message = safeMessage(ex.getMessage(), "Không thể lưu sản phẩm");
            bindingResult.reject("product.error", "Không thể lưu sản phẩm: " + message);
        } catch (Exception ex) {
            bindingResult.reject("product.error", "Lỗi khi upload hình ảnh: " + ex.getMessage());
        }

        return handleValidationErrors(model, null);
    }

    @PostMapping("/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("productForm") ProductForm productForm,
                                BindingResult bindingResult,
                                @RequestParam(value = "images", required = false) MultipartFile[] images,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(model, id);
        }

        try {
            productService.updateProduct(id, mapToEntity(productForm), productForm.getCategoryId());
            // Upload hình ảnh mới nếu có
            if (images != null && images.length > 0) {
                productService.uploadProductImages(id, images);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công");
            return "redirect:/admin/products";
        } catch (EntityNotFoundException | IllegalArgumentException ex) {
            bindingResult.reject("product.error", safeMessage(ex.getMessage(), "Không thể cập nhật sản phẩm"));
        } catch (DataIntegrityViolationException ex) {
            String message = safeMessage(ex.getMessage(), "Không thể cập nhật sản phẩm");
            bindingResult.reject("product.error", "Không thể cập nhật sản phẩm: " + message);
        } catch (Exception ex) {
            bindingResult.reject("product.error", "Lỗi khi upload hình ảnh: " + ex.getMessage());
        }

        return handleValidationErrors(model, id);
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm");
        } catch (EntityNotFoundException | DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", safeMessage(ex.getMessage(), "Không thể xóa sản phẩm"));
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/categories")
    public String createCategory(@Valid @ModelAttribute("categoryForm") CategoryForm categoryForm,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            if (!model.containsAttribute("productForm")) {
                model.addAttribute("productForm", new ProductForm());
            }
            model.addAttribute("products", productService.getAllProducts());
            return "admin/products";
        }

        try {
            categoryService.createCategory(
                    categoryForm.getName(),
                    categoryForm.getDescription(),
                    categoryForm.getParentId(),
                    categoryForm.isActive()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công");
            return "redirect:/admin/products";
        } catch (EntityNotFoundException | IllegalArgumentException ex) {
            bindingResult.reject("category.error", safeMessage(ex.getMessage(), "Không thể lưu danh mục"));
        } catch (DataIntegrityViolationException ex) {
            bindingResult.reject("category.error", safeMessage(ex.getMessage(), "Không thể lưu danh mục"));
        }

        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", new ProductForm());
        }
        model.addAttribute("products", productService.getAllProducts());
        return "admin/products";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", safeMessage(ex.getMessage(), "Danh mục không tồn tại"));
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", safeMessage(ex.getMessage(), "Không thể xóa danh mục đang chứa sản phẩm"));
        }
        return "redirect:/admin/products";
    }

    private String handleValidationErrors(Model model, Long editingProductId) {
        model.addAttribute("products", productService.getAllProducts());
        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new CategoryForm());
        }
        if (editingProductId != null) {
            model.addAttribute("editingProductId", editingProductId);
        }
        return "admin/products";
    }

    private Product mapToEntity(ProductForm form) {
        Product product = new Product();
        product.setId(form.getId());
        product.setName(form.getName());
        product.setSlug(form.getSlug());
        product.setDescription(form.getDescription());
        product.setBasePrice(form.getBasePrice());
        product.setHasVariants(form.isHasVariants());
        product.setStockQuantity(form.getStockQuantity() != null ? form.getStockQuantity() : 0);
        product.setActive(form.isActive());
        product.setBrand(form.getBrand());
        product.setWeight(form.getWeight());
        product.setMetaTitle(form.getMetaTitle());
        product.setMetaDescription(form.getMetaDescription());
        return product;
    }

    private ProductForm mapToForm(Product product) {
        ProductForm form = new ProductForm();
        form.setId(product.getId());
        form.setName(product.getName());
        form.setSlug(product.getSlug());
        form.setDescription(product.getDescription());
        form.setBasePrice(product.getBasePrice());
        form.setHasVariants(product.isHasVariants());
        form.setStockQuantity(product.getStockQuantity());
        form.setActive(product.isActive());
        form.setBrand(product.getBrand());
        form.setWeight(product.getWeight());
        form.setMetaTitle(product.getMetaTitle());
        form.setMetaDescription(product.getMetaDescription());
        if (product.getCategory() != null) {
            form.setCategoryId(product.getCategory().getId());
        }
        return form;
    }

    @NonNull
    private String safeMessage(@Nullable String candidate, @NonNull String fallback) {
        return candidate != null ? candidate : fallback;
    }
}

