package com.smartshop.repository;

import com.smartshop.entity.user.User;
import com.smartshop.entity.voucher.UserVoucher;
import com.smartshop.entity.voucher.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    Optional<UserVoucher> findByUserAndVoucher(User user, Voucher voucher);
}


