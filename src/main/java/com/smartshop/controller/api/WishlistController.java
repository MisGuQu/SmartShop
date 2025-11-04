package com.smartshop.controller.api;

import com.smartshop.dto.wishlist.AddToWishlistRequest;
import com.smartshop.dto.wishlist.WishlistItemDTO;
import com.smartshop.entity.user.User;
import com.smartshop.response.ApiResponse;
import com.smartshop.service.WishlistService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * 💖 Lấy danh sách sản phẩm yêu thích của người dùng hiện tại
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistItemDTO>>> getWishlist(Authentication auth) {
        User user = (User) auth.getPrincipal();
        List<WishlistItemDTO> wishlist = wishlistService.getWishlist(user.getId());
        return ResponseEntity.ok(ApiResponse.success(wishlist));
    }

    /**
     * ➕ Thêm sản phẩm vào danh sách yêu thích
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Void>> addItem(
            @RequestBody AddToWishlistRequest request,
            Authentication auth) {
        User user = (User) auth.getPrincipal();
        wishlistService.addItem(user.getId(), request.getProductId(), request.getVariantId());
        return ResponseEntity.ok(ApiResponse.success(null, "Đã thêm vào danh sách yêu thích"));
    }

    /**
     * ❌ Xóa sản phẩm khỏi danh sách yêu thích
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable Long productId,
            @RequestParam(required = false) Long variantId,
            Authentication auth) {
        User user = (User) auth.getPrincipal();
        wishlistService.removeItem(user.getId(), productId, variantId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa khỏi danh sách yêu thích"));
    }
}
