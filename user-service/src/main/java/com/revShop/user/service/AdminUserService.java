package com.revShop.user.service;

import com.revShop.user.dto.AdminUserResponse;
import com.revShop.user.entity.Role;
import com.revShop.user.entity.User;
import com.revShop.user.exception.ApiException;
import com.revShop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers(String callerRole, Role role) {
        requireAdmin(callerRole);
        List<User> users = (role == null)
                ? userRepository.findAllByOrderByCreatedAtDesc()
                : userRepository.findByRoleOrderByCreatedAtDesc(role);
        return users.stream().map(AdminUserResponse::from).toList();
    }

    @Transactional
    public AdminUserResponse updateStatus(Long id, boolean active, Long callerId, String callerRole) {
        requireAdmin(callerRole);
        if (id.equals(callerId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You can't block or unblock your own account");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User " + id + " not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Admin accounts can't be blocked");
        }
        user.setActive(active);
        return AdminUserResponse.from(user);
    }

    private void requireAdmin(String callerRole) {
        if (!"ADMIN".equals(callerRole)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admins can manage users");
        }
    }
}