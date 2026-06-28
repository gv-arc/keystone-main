package com.keystone.service;

import com.keystone.domain.entity.User;
import com.keystone.domain.enums.Role;
import com.keystone.dto.request.UserRequest;
import com.keystone.dto.response.PageResponse;
import com.keystone.dto.response.UserResponse;
import com.keystone.exception.BusinessException;
import com.keystone.exception.ResourceNotFoundException;
import com.keystone.mapper.EntityMapper;
import com.keystone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return PageResponse.<UserResponse>builder()
                .content(page.getContent().stream().map(mapper::toUserResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered", HttpStatus.CONFLICT);
        }
        if (request.getRole() == Role.CUSTOMER && request.getCustomerId() == null) {
            throw new BusinessException("Customer users must be linked to a customer",
                    HttpStatus.BAD_REQUEST);
        }
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(
                        request.getPassword() != null ? request.getPassword() : "password123"))
                .fullName(request.getFullName())
                .role(request.getRole())
                .customer(request.getCustomerId() != null
                        ? customerService.findCustomer(request.getCustomerId()) : null)
                .active(true)
                .build();
        return mapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getCustomerId() != null) {
            user.setCustomer(customerService.findCustomer(request.getCustomerId()));
        }
        return mapper.toUserResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listTechnicians() {
        return userRepository.findByRoleAndActiveTrue(Role.TECHNICIAN).stream()
                .map(mapper::toUserResponse)
                .toList();
    }
}
