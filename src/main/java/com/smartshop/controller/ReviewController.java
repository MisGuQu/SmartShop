package com.smartshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/review")
public class ReviewController {

    @GetMapping("/product/{productId}")
    public String listReviews(@PathVariable Long productId, Model model) {
        // TODO: hiển thị đánh giá theo sản phẩm
        return "review/list";
    }

    @PostMapping("/add")
    public String addReview(@RequestParam Long productId, @RequestParam String content, @RequestParam int rating) {
        // TODO: thêm đánh giá
        return "redirect:/products/" + productId;
    }

    @GetMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        // TODO: xóa đánh giá
        return "redirect:/";
    }
}
