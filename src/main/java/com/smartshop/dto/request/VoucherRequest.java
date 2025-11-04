package com.smartshop.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherRequest {
    private String code;
    private String description;
    private String discountType; // PERCENTAGE or FIXED_AMOUNT
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderAmount;
    private Integer usageLimit;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
