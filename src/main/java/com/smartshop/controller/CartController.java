package com.smartshop.controller;

import com.smartshop.dto.cart.CartSummaryView;
import com.smartshop.dto.order.CheckoutRequest;
import com.smartshop.entity.enums.PaymentMethod;
import com.smartshop.entity.enums.ShippingMethod;
import com.smartshop.entity.user.User;
import com.smartshop.service.CartService;
import com.smartshop.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CheckoutService checkoutService;

    @GetMapping
    public String viewCart(@AuthenticationPrincipal User user,
                           Model model) {
        if (user == null) {
            return "redirect:/auth/login?redirect=/cart";
        }
        CartSummaryView summary = cartService.getCartSummary(user.getId());
        model.addAttribute("cart", summary);
        return "cart/view";
    }

    @GetMapping("/checkout")
    public String checkoutPage(@AuthenticationPrincipal User user,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (user == null) {
            return "redirect:/auth/login?redirect=/cart/checkout";
        }
        CartSummaryView summary = cartService.getCartSummary(user.getId());
        if (summary.isEmpty()) {
            redirectAttributes.addFlashAttribute("cartMessage", "Giỏ hàng của bạn đang trống");
            return "redirect:/cart";
        }
        if (!model.containsAttribute("checkoutRequest")) {
            model.addAttribute("checkoutRequest", new CheckoutRequest());
        }
        model.addAttribute("cart", summary);
        model.addAttribute("shippingMethods", ShippingMethod.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "cart/checkout";
    }

    @PostMapping("/checkout")
    public String handleCheckout(@AuthenticationPrincipal User user,
                                 @Valid @ModelAttribute("checkoutRequest") CheckoutRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (user == null) {
            return "redirect:/auth/login?redirect=/cart/checkout";
        }

        CartSummaryView summary = cartService.getCartSummary(user.getId());
        if (summary.isEmpty()) {
            redirectAttributes.addFlashAttribute("cartMessage", "Giỏ hàng của bạn đang trống");
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("cart", summary);
            model.addAttribute("shippingMethods", ShippingMethod.values());
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "cart/checkout";
        }

        checkoutService.placeOrder(user.getId(), request);
        redirectAttributes.addFlashAttribute("checkoutSuccess", "Đơn hàng của bạn đã được tạo thành công!");
        return "redirect:/";
    }
}
