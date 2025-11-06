package com.smartshop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminService {

    public long countUsers() {
        // TODO: Đếm số lượng người dùng
        return 0;
    }

    public long countOrders() {
        // TODO: Đếm số lượng đơn hàng
        return 0;
    }

    public long countProducts() {
        // TODO: Đếm số lượng sản phẩm
        return 0;
    }

    public double getTotalRevenue() {
        // TODO: Tính tổng doanh thu
        return 0.0;
    }

    public void deactivateUser(Long userId) {
        // TODO: Khóa tài khoản người dùng
    }
}
