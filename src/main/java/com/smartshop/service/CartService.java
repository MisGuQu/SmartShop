package com.smartshop.service;

import com.smartshop.entity.cart.ShoppingCart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {

    public ShoppingCart getCartByUserId(Long userId) {
        // TODO: Lấy giỏ hàng của user
        return new ShoppingCart();
    }

    public ShoppingCart addItem(Long userId, Long productId, int quantity) {
        // TODO: Thêm sản phẩm vào giỏ
        return new ShoppingCart();
    }

    public ShoppingCart updateItem(Long userId, Long cartItemId, int quantity) {
        // TODO: Cập nhật số lượng sản phẩm
        return new ShoppingCart();
    }

    public ShoppingCart removeItem(Long userId, Long cartItemId) {
        // TODO: Xóa 1 sản phẩm khỏi giỏ hàng
        return new ShoppingCart();
    }

    public void clearCart(Long userId) {
        // TODO: Xóa toàn bộ giỏ hàng
    }
}
