package com.keystone.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String role;
    private Long userId;
    private String fullName;
    private Long customerId;
    private String email;
    private long expiresIn;
}
