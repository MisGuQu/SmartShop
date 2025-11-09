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

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
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
