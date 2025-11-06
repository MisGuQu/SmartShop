package com.smartshop.service;

import com.smartshop.entity.voucher.Voucher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VoucherService {

    public List<Voucher> getAllVouchers() {
        // TODO: Danh sách tất cả voucher
        return List.of();
    }

    public Optional<Voucher> getVoucherByCode(String code) {
        // TODO: Tìm voucher theo mã
        return Optional.empty();
    }

    public boolean applyVoucher(Long userId, String code) {
        // TODO: Áp dụng voucher cho người dùng
        return true;
    }

    public Voucher createVoucher(Voucher voucher) {
        // TODO: Tạo voucher mới
        return voucher;
    }

    public void deleteVoucher(Long id) {
        // TODO: Xóa voucher
    }
}
