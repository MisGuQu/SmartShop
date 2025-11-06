package com.smartshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    @GetMapping
    public String viewCart() {
        return "cart/view";
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId) {
        return "redirect:/cart";
    }

    @PostMapping("/remove/{itemId}")
    public String removeFromCart(@PathVariable Long itemId) {
        return "redirect:/cart";
    }

    @PostMapping("/update/{itemId}")
    public String updateCartItem(@PathVariable Long itemId) {
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkoutPage() {
        return "cart/checkout";
    }
}
