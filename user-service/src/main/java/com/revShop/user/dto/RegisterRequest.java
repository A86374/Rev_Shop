package com.revShop.user.dto;

import com.revShop.user.entity.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "\\d{10}", message = "must be exactly 10 digits") String phone,
        @NotBlank @Size(min = 8, message = "must be at least 8 characters") String password,
        Role role,              // empty = BUYER
        String businessName     // sellers only
) {}