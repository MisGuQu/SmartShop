package com.smartshop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    public boolean register(String email, String password) {
        // TODO: Xử lý đăng ký tài khoản
        return true;
    }

    public boolean login(String email, String password) {
        // TODO: Kiểm tra đăng nhập
        return true;
    }

    public void logout() {
        // TODO: Xử lý đăng xuất
    }

    public boolean resetPassword(String email) {
        // TODO: Gửi link đặt lại mật khẩu
        return true;
    }
}
