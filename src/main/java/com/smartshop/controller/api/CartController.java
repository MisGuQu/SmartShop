package com.smartshop.controller.api;

import com.smartshop.dto.cart.AddToCartRequest;
import com.smartshop.dto.cart.UpdateCartItemRequest;
import com.smartshop.dto.cart.CartDTO;
import com.smartshop.entity.User;
import com.smartshop.response.ApiResponse;
import com.smartshop.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CartController {

    private final CartService cartService;

    /**
     * 🛒 Lấy giỏ hàng của người dùng hiện tại
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartDTO>> getCart(Authentication auth) {
        User user = (User) auth.getPrincipal();
        CartDTO cart = cartService.getCart(user.getId());
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    /**
     * ➕ Thêm sản phẩm vào giỏ hàng
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartDTO>> addItem(
            @Valid @RequestBody AddToCartRequest request,
            Authentication auth) {
        User user = (User) auth.getPrincipal();
        CartDTO cart = cartService.addItem(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Thêm vào giỏ hàng thành công"));
    }

    /**
     * 🔁 Cập nhật số lượng sản phẩm trong giỏ
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CartDTO>> updateItem(
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication auth) {
        User user = (User) auth.getPrincipal();
        CartDTO cart = cartService.updateItem(user.getId(), request.getCartItemId(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(cart, "Cập nhật giỏ hàng thành công"));
    }

    /**
     * ❌ Xóa 1 sản phẩm trong giỏ
     */
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeItem(
            @PathVariable Long cartItemId,
            Authentication auth) {
        User user = (User) auth.getPrincipal();
        CartDTO cart = cartService.removeItem(user.getId(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Xóa sản phẩm khỏi giỏ hàng thành công"));
    }

    /**
     * 🧹 Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication auth) {
        User user = (User) auth.getPrincipal();
        cartService.clearCart(user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa giỏ hàng thành công"));
    }
}
