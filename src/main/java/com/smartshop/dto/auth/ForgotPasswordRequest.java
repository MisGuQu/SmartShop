package com.smartshop.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "Email không được bỏ trống")
    @Email(message = "Email không hợp lệ")
    private String email;
}

