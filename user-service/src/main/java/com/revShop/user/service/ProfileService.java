package com.revShop.user.service;

import com.revShop.user.dto.ChangePasswordRequest;
import com.revShop.user.dto.ProfileResponse;
import com.revShop.user.dto.UpdateProfileRequest;
import com.revShop.user.entity.Role;
import com.revShop.user.entity.User;
import com.revShop.user.exception.ApiException;
import com.revShop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // US-04: my profile
    public ProfileResponse get(Long userId) {
        return ProfileResponse.from(find(userId));
    }

    @Transactional
    public ProfileResponse update(Long userId, UpdateProfileRequest req) {
        User user = find(userId);
        user.setFullName(req.fullName().trim());
        user.setPhone(req.phone());

        // US-02 still holds: a seller always has a business name, nobody else has one
        if (user.getRole() == Role.SELLER) {
            if (req.businessName() == null || req.businessName().isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Business name is required for sellers");
            }
            user.setBusinessName(req.businessName().trim());
        }
        return ProfileResponse.from(userRepository.save(user));
    }

    // 400, not 401, for a wrong password: a 401 would log the user out in Angular
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = find(userId);
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is wrong");
        }
        if (passwordEncoder.matches(req.newPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "New password must be different from the current one");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    private User find(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}