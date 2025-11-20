package com.smartshop.controller;

import com.smartshop.dto.voucher.VoucherRequest;
import com.smartshop.dto.voucher.VoucherResponse;
import com.smartshop.service.VoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    // ✅ Xem danh sách tất cả voucher (Admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VoucherResponse>> getAll() {
        return ResponseEntity.ok(voucherService.getAll());
    }

    // ✅ Chi tiết 1 voucher (Admin)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getById(id));
    }

    // ✅ Thêm mã giảm giá
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherResponse> create(@RequestBody VoucherRequest req) {
        return ResponseEntity.ok(voucherService.create(req));
    }

    // ✅ Sửa thông tin voucher
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherResponse> update(@PathVariable Long id,
                                                  @RequestBody VoucherRequest req) {
        return ResponseEntity.ok(voucherService.update(id, req));
    }

    // ✅ Xóa voucher (xóa cứng)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        voucherService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Vô hiệu hóa voucher (soft)
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherResponse> disable(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.disable(id));
    }
}


