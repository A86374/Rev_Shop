package com.revShop.user.controller;

import com.revShop.user.dto.ChangePasswordRequest;
import com.revShop.user.dto.ProfileResponse;
import com.revShop.user.dto.UpdateProfileRequest;
import com.revShop.user.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// /api/users/** is already routed to user-service; the gateway puts the user id from the token in X-User-Id
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ProfileResponse get(@RequestHeader("X-User-Id") Long userId) {
        return profileService.get(userId);
    }

    @PutMapping
    public ProfileResponse update(@RequestHeader("X-User-Id") Long userId,
                                  @Valid @RequestBody UpdateProfileRequest request) {
        return profileService.update(userId, request);
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestHeader("X-User-Id") Long userId,
                               @Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(userId, request);
    }
}