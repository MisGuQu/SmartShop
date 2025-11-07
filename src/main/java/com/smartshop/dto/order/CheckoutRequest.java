package com.smartshop.dto.order;

import com.smartshop.entity.enums.PaymentMethod;
import com.smartshop.entity.enums.ShippingMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotBlank(message = "Vui lòng nhập họ và tên")
    private String customerName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String customerPhone;

    @Email(message = "Email không hợp lệ")
    private String customerEmail;

    @NotBlank(message = "Vui lòng nhập địa chỉ giao hàng")
    private String shippingAddress;

    private String shippingCity;
    private String shippingDistrict;
    private String shippingWard;

    @NotNull(message = "Vui lòng chọn phương thức giao hàng")
    private ShippingMethod shippingMethod;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    private String customerNote;
}

