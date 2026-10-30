package com.revShop.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// email is the login, so it can't be changed here
public record UpdateProfileRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Pattern(regexp = "\\d{10}", message = "must be exactly 10 digits") String phone,
        @Size(max = 150) String businessName     // sellers only
) {}