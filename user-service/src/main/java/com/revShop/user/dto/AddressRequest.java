package com.revShop.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Pattern(regexp = "\\d{10}", message = "must be exactly 10 digits") String phone,
        @NotBlank @Size(max = 200) String line1,
        @Size(max = 200) String line2,
        @NotBlank @Size(max = 80) String city,
        @NotBlank @Size(max = 80) String state,
        @NotBlank @Pattern(regexp = "[1-9]\\d{5}", message = "must be a 6-digit PIN code") String pincode
) {}