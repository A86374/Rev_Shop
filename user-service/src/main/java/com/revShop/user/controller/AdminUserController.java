package com.revShop.user.controller;

import com.revShop.user.dto.AdminUserResponse;
import com.revShop.user.dto.UpdateUserStatusRequest;
import com.revShop.user.entity.Role;
import com.revShop.user.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    // GET /api/admin/users            -> everyone
    // GET /api/admin/users?role=SELLER -> only sellers
    @GetMapping
    public List<AdminUserResponse> listUsers(@RequestHeader("X-User-Role") String callerRole,
                                             @RequestParam(value = "role", required = false) Role role) {
        return adminUserService.listUsers(callerRole, role);
    }

    @PatchMapping("/{id}/status")
    public AdminUserResponse updateStatus(@PathVariable("id") Long id,
                                          @RequestHeader("X-User-Id") Long callerId,
                                          @RequestHeader("X-User-Role") String callerRole,
                                          @Valid @RequestBody UpdateUserStatusRequest request) {
        return adminUserService.updateStatus(id, request.active(), callerId, callerRole);
    }
}