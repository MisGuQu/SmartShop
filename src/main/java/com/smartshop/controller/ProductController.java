package com.smartshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
public class ProductController {

    @GetMapping
    public String listProducts(Model model) {
        // TODO: lấy danh sách sản phẩm
        return "product/list";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        // TODO: xem chi tiết sản phẩm
        return "product/detail";
    }

    @GetMapping("/add")
    public String addProductForm() {
        return "product/add";
    }

    @PostMapping("/add")
    public String addProduct() {
        // TODO: thêm sản phẩm mới
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        // TODO: hiển thị form sửa
        return "product/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id) {
        // TODO: cập nhật sản phẩm
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        // TODO: xóa sản phẩm
        return "redirect:/products";
    }
}
