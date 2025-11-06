package com.smartshop.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank
    private String token;

    @NotBlank(message = "Mật khẩu không được bỏ trống")
    @Size(min = 8, max = 64, message = "Mật khẩu phải từ 8-64 ký tự")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được bỏ trống")
    private String confirmPassword;
}

