package com.keystone.dto.request;

import com.keystone.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank
    @Email
    private String email;

    private String password;

    @NotBlank
    private String fullName;

    @NotNull
    private Role role;

    private Long customerId;
}
