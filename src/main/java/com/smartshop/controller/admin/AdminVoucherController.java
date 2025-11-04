package com.smartshop.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartshop.dto.voucher.CreateVoucherRequest;
import com.smartshop.dto.voucher.UpdateVoucherRequest;
import com.smartshop.dto.voucher.VoucherDTO;
import com.smartshop.dto.voucher.VoucherUsageDTO;
import com.smartshop.response.ApiResponse;
import com.smartshop.service.admin.AdminVoucherService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVoucherController {

    private final AdminVoucherService voucherService;

    @PostMapping
    public ResponseEntity<ApiResponse<VoucherDTO>> create(@Valid @RequestBody CreateVoucherRequest request) {
        VoucherDTO voucher = voucherService.createVoucher(request);
        return ResponseEntity.ok(ApiResponse.success(voucher, "Tạo voucher thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VoucherDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<VoucherDTO> vouchers = voucherService.getAllVouchers(page, size);
        return ResponseEntity.ok(ApiResponse.success(vouchers));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVoucherRequest request) {
        VoucherDTO voucher = voucherService.updateVoucher(id, request);
        return ResponseEntity.ok(ApiResponse.success(voucher));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa voucher thành công"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<List<VoucherUsageDTO>>> getStats() {
        List<VoucherUsageDTO> stats = voucherService.getVoucherUsageStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
