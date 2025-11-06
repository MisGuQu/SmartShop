package com.smartshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping({"", "/", "/index"})
    public String adminDashboard() {
        return "admin/index";
    }

    @GetMapping("/users")
    public String manageUsers() {
        return "admin/users";
    }

    @GetMapping("/products")
    public String manageProducts() {
        return "admin/products";
    }

    @GetMapping("/orders")
    public String manageOrders() {
        return "admin/orders";
    }

    @GetMapping("/vouchers")
    public String manageVouchers() {
        return "admin/vouchers";
    }

    @GetMapping("/reviews")
    public String manageReviews() {
        return "admin/reviews";
    }
}
