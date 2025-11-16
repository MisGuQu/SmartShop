package com.smartshop.controller;

import com.smartshop.entity.cart.CartItem;
import com.smartshop.entity.user.User;
import com.smartshop.service.ProductService;
import com.smartshop.service.WishlistService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final ProductService productService;

    @Value("${CLOUD_NAME:Root}")
    private String cloudName;

    // ============================================================
    // GET - Hiển thị trang wishlist
    // ============================================================

    @GetMapping
    public String wishlistView(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/auth/login?redirect=/wishlist";
        }

        List<CartItem> items = wishlistService.getWishlistItems(user.getId());
        List<WishlistItemView> itemViews = items.stream()
                .map(item -> new WishlistItemView(item,
                        productService.getPrimaryImage(item.getProduct().getId())
                                .map(productImage -> buildCloudinaryUrl(productImage.getPublicId()))
                                .orElse(null)))
                .collect(Collectors.toList());

        model.addAttribute("items", itemViews);
        return "wishlist/wishlist";
    }

    // ============================================================
    // POST - Thêm/xóa sản phẩm khỏi wishlist
    // ============================================================

    @PostMapping("/add")
    public String addToWishlist(@AuthenticationPrincipal User user,
                                @RequestParam Long productId,
                                @RequestParam(required = false) Long variantId,
                                RedirectAttributes redirectAttributes,
                                @RequestHeader(value = "Referer", required = false) String referer) {
        if (user == null) {
            return "redirect:/auth/login?redirect=/wishlist";
        }
        try {
            wishlistService.addToWishlist(user.getId(), productId, variantId);
            redirectAttributes.addFlashAttribute("wishlistMessage", "Đã lưu sản phẩm vào danh sách yêu thích.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("wishlistMessage", ex.getMessage());
        }
        return referer != null ? "redirect:" + referer : "redirect:/wishlist";
    }

    @PostMapping("/remove")
    public String removeFromWishlist(@AuthenticationPrincipal User user,
                                     @RequestParam Long itemId,
                                     RedirectAttributes redirectAttributes,
                                     @RequestHeader(value = "Referer", required = false) String referer) {
        if (user == null) {
            return "redirect:/auth/login?redirect=/wishlist";
        }
        try {
            wishlistService.removeFromWishlist(user.getId(), itemId);
            redirectAttributes.addFlashAttribute("wishlistMessage", "Đã xóa sản phẩm khỏi danh sách yêu thích.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("wishlistMessage", ex.getMessage());
        }
        return referer != null ? "redirect:" + referer : "redirect:/wishlist";
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private String buildCloudinaryUrl(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return null;
        }
        if (publicId.startsWith("http")) {
            return publicId;
        }
        String name = (cloudName != null && !cloudName.isBlank()) ? cloudName : "Root";
        return "https://res.cloudinary.com/" + name + "/image/upload/" + publicId;
    }

    // ============================================================
    // INNER CLASSES
    // ============================================================

    @Getter
    public static class WishlistItemView {
        private final CartItem item;
        private final String imageUrl;

        public WishlistItemView(CartItem item, String imageUrl) {
            this.item = Objects.requireNonNull(item);
            this.imageUrl = imageUrl;
        }
    }
}
