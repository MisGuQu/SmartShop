package com.smartshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/vouchers")
public class VoucherController {

    @GetMapping
    public String listVouchers() {
        return "voucher/list";
    }

    @GetMapping("/create")
    public String createVoucherForm() {
        return "voucher/create";
    }

    @PostMapping("/create")
    public String createVoucher() {
        return "redirect:/vouchers";
    }

    @GetMapping("/edit/{id}")
    public String editVoucherForm(@PathVariable Long id) {
        return "voucher/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateVoucher(@PathVariable Long id) {
        return "redirect:/vouchers";
    }

    @GetMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable Long id) {
        return "redirect:/vouchers";
    }
}
