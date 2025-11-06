package com.smartshop.dto.auth;

import com.smartshop.entity.user.User;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResult {
    String token;
    User user;
}

