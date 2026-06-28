package com.keystone.service;

import com.keystone.dto.request.LoginRequest;
import com.keystone.dto.response.AuthResponse;
import com.keystone.security.JwtTokenProvider;
import com.keystone.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String token = jwtTokenProvider.generateToken(principal);
        return AuthResponse.builder()
                .token(token)
                .role(principal.getRole())
                .userId(principal.getId())
                .fullName(principal.getFullName())
                .customerId(principal.getCustomerId())
                .email(principal.getEmail())
                .expiresIn(jwtTokenProvider.getExpirationMs())
                .build();
    }
}
