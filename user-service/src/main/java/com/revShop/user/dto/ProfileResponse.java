package com.revShop.user.dto;

import com.revShop.user.entity.User;

import java.time.LocalDateTime;

// never includes the password hash
public record ProfileResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String role,
        String businessName,
        LocalDateTime createdAt
) {
    public static ProfileResponse from(User u) {
        return new ProfileResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(),
                u.getRole().name(), u.getBusinessName(), u.getCreatedAt());
    }
}