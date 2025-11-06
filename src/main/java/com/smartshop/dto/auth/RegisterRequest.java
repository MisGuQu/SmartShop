package com.smartshop.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 120, message = "Họ tên quá dài")
    private String fullName;

    @NotBlank(message = "Email không được bỏ trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được bỏ trống")
    @Size(min = 8, max = 64, message = "Mật khẩu phải từ 8-64 ký tự")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được bỏ trống")
    private String confirmPassword;
}

