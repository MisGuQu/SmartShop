package com.smartshop.service;

import com.smartshop.entity.voucher.UserVoucher;
import com.smartshop.entity.voucher.Voucher;
import com.smartshop.repository.UserVoucherRepository;
import com.smartshop.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public Optional<Voucher> getVoucherByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return Optional.empty();
        }
        return voucherRepository.findByCodeAndIsActiveTrue(code.trim());
    }

    public Voucher createVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }

    public List<UserVoucher> getUserVouchers(Long userId) {
        return userVoucherRepository.findByUserIdAndIsUsedFalse(userId);
    }
}
