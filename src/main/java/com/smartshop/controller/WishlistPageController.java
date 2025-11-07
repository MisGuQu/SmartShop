package com.smartshop.controller;

import com.smartshop.entity.cart.Wishlist;
import com.smartshop.entity.cart.WishlistItem;
import com.smartshop.entity.product.ProductImage;
import com.smartshop.entity.user.User;
import com.smartshop.service.ProductService;
import com.smartshop.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class WishlistPageController {

    private final WishlistService wishlistService;
    private final ProductService productService;

    @Value("${CLOUD_NAME:Root}")
    private String cloudName;

    @GetMapping("/wishlist")
    public String wishlistView(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/auth/login?redirect=/wishlist";
        }
        Wishlist wishlist = wishlistService.getWishlistByUser(user.getId());
        model.addAttribute("wishlist", wishlist);
        model.addAttribute("items", wishlist.getItems().stream()
                .map(item -> {
                    String imageUrl = productService.getPrimaryImage(item.getProduct().getId())
                            .map(ProductImage::getPublicId)
                            .map(this::buildCloudinaryUrl)
                            .orElse(null);
                    return new WishlistItemView(item, imageUrl);
                })
                .collect(Collectors.toList()));
        return "wishlist/wishlist";
    }

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

    public static class WishlistItemView {
        private final WishlistItem item;
        private final String imageUrl;

        public WishlistItemView(WishlistItem item, String imageUrl) {
            this.item = item;
            this.imageUrl = imageUrl;
        }

        public WishlistItem getItem() {
            return item;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }
}

