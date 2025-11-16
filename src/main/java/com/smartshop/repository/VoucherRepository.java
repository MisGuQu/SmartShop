package com.smartshop.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartshop.entity.voucher.Voucher;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCodeAndIsActiveTrue(String code);

    @Query("SELECT v FROM Voucher v WHERE " +
           "v.isActive = true AND " +
           "(v.startDate IS NULL OR v.startDate <= :now) AND " +
           "(v.endDate IS NULL OR v.endDate >= :now)")
    List<Voucher> findActiveVouchers(@Param("now") LocalDateTime now);
}
