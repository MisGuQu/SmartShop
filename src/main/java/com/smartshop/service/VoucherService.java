package com.smartshop.service;

import com.smartshop.dto.voucher.VoucherRequest;
import com.smartshop.dto.voucher.VoucherResponse;
import com.smartshop.entity.product.Category;
import com.smartshop.entity.voucher.Voucher;
import com.smartshop.repository.CategoryRepository;
import com.smartshop.repository.VoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final CategoryRepository categoryRepository;

    public VoucherService(VoucherRepository voucherRepository,
                          CategoryRepository categoryRepository) {
        this.voucherRepository = voucherRepository;
        this.categoryRepository = categoryRepository;
    }

    // ✅ Xem danh sách tất cả voucher
    public List<VoucherResponse> getAll() {
        return voucherRepository.findAll()
                .stream()
                .map(VoucherResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public VoucherResponse getById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        return VoucherResponse.fromEntity(voucher);
    }

    // ✅ Thêm mã giảm giá (Voucher Code)
    public VoucherResponse create(VoucherRequest req) {
        if (voucherRepository.existsByCode(req.getCode())) {
            throw new RuntimeException("Voucher code already exists");
        }

        Category category = null;
        if (req.getCategoryId() != null) {
            category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        }

        Voucher voucher = Voucher.builder()
                .code(req.getCode())
                .type(req.getType())
                .value(req.getValue())
                .minOrder(req.getMinOrder())
                .category(category)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .build();

        return VoucherResponse.fromEntity(voucherRepository.save(voucher));
    }

    // ✅ Sửa thông tin voucher
    public VoucherResponse update(Long id, VoucherRequest req) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        if (req.getCode() != null && !req.getCode().equals(voucher.getCode())) {
            if (voucherRepository.existsByCode(req.getCode())) {
                throw new RuntimeException("Voucher code already exists");
            }
            voucher.setCode(req.getCode());
        }

        if (req.getType() != null) voucher.setType(req.getType());
        if (req.getValue() != null) voucher.setValue(req.getValue());
        if (req.getMinOrder() != null) voucher.setMinOrder(req.getMinOrder());

        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            voucher.setCategory(category);
        } else {
            voucher.setCategory(null);
        }

        if (req.getStartDate() != null) voucher.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) voucher.setEndDate(req.getEndDate());
        if (req.getIsActive() != null) voucher.setIsActive(req.getIsActive());

        return VoucherResponse.fromEntity(voucherRepository.save(voucher));
    }

    // ✅ Xóa hoặc vô hiệu hóa voucher

    // Xóa cứng
    public void delete(Long id) {
        voucherRepository.deleteById(id);
    }

    // Vô hiệu hóa (soft disable)
    public VoucherResponse disable(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        voucher.setIsActive(false);
        return VoucherResponse.fromEntity(voucherRepository.save(voucher));
    }
}


