package com.smartshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartshop.entity.UserVoucher;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    List<UserVoucher> findByUserId(Long userId);
    List<UserVoucher> findByUserIdAndIsUsedFalse(Long userId);
    Optional<UserVoucher> findByUserIdAndVoucherId(Long userId, Long voucherId);
    int countByVoucherIdAndIsUsedTrue(Long voucherId);
}
