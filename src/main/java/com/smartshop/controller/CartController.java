package com.smartshop.controller;

import com.smartshop.dto.cart.AddToCartRequest;
import com.smartshop.dto.cart.CartSummaryView;
import com.smartshop.dto.cart.UpdateCartItemRequest;
import com.smartshop.dto.order.CheckoutRequest;
import com.smartshop.entity.enums.PaymentMethod;
import com.smartshop.entity.enums.ShippingMethod;
import com.smartshop.entity.user.User;
import com.smartshop.service.CartService;
import com.smartshop.service.CheckoutService;
import com.smartshop.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final PaymentService paymentService;

    // ============================================================
    // GET - Xem giỏ hàng
    // ============================================================

    @GetMapping
    public String viewCart(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/auth/login?redirect=/cart";
        }
        CartSummaryView summary = cartService.getCartSummary(user.getId());
        model.addAttribute("cart", summary);
        return "cart/view";
    }

    // ============================================================
    // GET - Trang checkout
    // ============================================================

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

    // ============================================================
    // POST - Thêm sản phẩm vào giỏ hàng
    // ============================================================

    @PostMapping("/add")
    public String addToCart(@AuthenticationPrincipal User user,
                            @Valid @ModelAttribute("addToCartRequest") AddToCartRequest request,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            @RequestHeader(value = "Referer", required = false) String referer) {
        if (user == null) {
            return "redirect:/auth/login?redirect=/cart";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("cartMessage", "Dữ liệu không hợp lệ. Vui lòng thử lại.");
            return referer != null ? "redirect:" + referer : "redirect:/cart";
        }

        try {
            cartService.addItem(user.getId(), request.getProductId(), request.getVariantId(), request.getQuantity());
            redirectAttributes.addFlashAttribute("cartMessage", "Đã thêm sản phẩm vào giỏ hàng.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("cartMessage", ex.getMessage());
        }
        return referer != null ? "redirect:" + referer : "redirect:/cart";
    }

    // ============================================================
    // POST - Cập nhật số lượng sản phẩm trong giỏ hàng
    // ============================================================

    @PostMapping("/update")
    public String updateCartItem(@AuthenticationPrincipal User user,
                                 @RequestParam Long itemId,
                                 @Valid @ModelAttribute("updateCartItemRequest") UpdateCartItemRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (user == null) {
            return "redirect:/auth/login?redirect=/cart";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("cartMessage", "Số lượng không hợp lệ. Vui lòng thử lại.");
            return "redirect:/cart";
        }

        try {
            cartService.updateItem(user.getId(), itemId, request.getQuantity());
            redirectAttributes.addFlashAttribute("cartMessage", "Đã cập nhật số lượng sản phẩm.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("cartMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }

    // ============================================================
    // POST - Xóa sản phẩm khỏi giỏ hàng
    // ============================================================

    @PostMapping("/remove")
    public String removeFromCart(@AuthenticationPrincipal User user,
                                  @RequestParam Long itemId,
                                  RedirectAttributes redirectAttributes) {
        if (user == null) {
            return "redirect:/auth/login?redirect=/cart";
        }

        try {
            cartService.removeItem(user.getId(), itemId);
            redirectAttributes.addFlashAttribute("cartMessage", "Đã xóa sản phẩm khỏi giỏ hàng.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("cartMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }

    // ============================================================
    // POST - Xóa toàn bộ giỏ hàng
    // ============================================================

    @PostMapping("/clear")
    public String clearCart(@AuthenticationPrincipal User user,
                            RedirectAttributes redirectAttributes) {
        if (user == null) {
            return "redirect:/auth/login?redirect=/cart";
        }

        try {
            cartService.clearCart(user.getId());
            redirectAttributes.addFlashAttribute("cartMessage", "Đã xóa toàn bộ giỏ hàng.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("cartMessage", "Không thể xóa giỏ hàng: " + ex.getMessage());
        }
        return "redirect:/cart";
    }

    // ============================================================
    // POST - Đặt hàng (Checkout)
    // ============================================================

    @PostMapping("/checkout")
    public String checkout(@AuthenticationPrincipal User user,
                           @Valid @ModelAttribute("checkoutRequest") CheckoutRequest request,
                           BindingResult bindingResult,
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

        if (bindingResult.hasErrors()) {
            model.addAttribute("cart", summary);
            model.addAttribute("shippingMethods", ShippingMethod.values());
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "cart/checkout";
        }

        try {
            var order = checkoutService.placeOrder(user.getId(), request);
            if (order.getPaymentMethod() == PaymentMethod.VNPAY || order.getPaymentMethod() == PaymentMethod.MOMO) {
                String redirectUrl = paymentService.initiateGatewayPayment(order);
                return "redirect:" + redirectUrl;
            }
            redirectAttributes.addFlashAttribute("orderMessage", "Đặt hàng thành công!");
            return "redirect:/orders/" + order.getId();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("cartMessage", ex.getMessage());
            return "redirect:/cart/checkout";
        }
    }
}
