package com.revShop.user.service;

import com.revShop.user.dto.AuthResponse;
import com.revShop.user.dto.LoginRequest;
import com.revShop.user.dto.RegisterRequest;
import com.revShop.user.entity.Role;
import com.revShop.user.entity.User;
import com.revShop.user.exception.ApiException;
import com.revShop.user.repository.UserRepository;
import com.revShop.user.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest req) {
        Role role = req.role() == null ? Role.BUYER : req.role();
        if (role == Role.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You can only register as BUYER or SELLER");
        }
        // US-02 (Hasitha): add the seller business-name rule here

        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = User.builder()
                .fullName(req.fullName().trim())
                .email(email)
                .phone(req.phone())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(role)
                .businessName(req.businessName())
                .build();

        return toResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Your account is blocked");
        }
        return toResponse(user);
    }

    private AuthResponse toResponse(User u) {
        return new AuthResponse(jwtUtil.generateToken(u), u.getId(), u.getFullName(),
                u.getEmail(), u.getRole().name());
    }
}