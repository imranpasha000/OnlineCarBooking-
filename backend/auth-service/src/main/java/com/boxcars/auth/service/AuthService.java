package com.boxcars.auth.service;

import com.boxcars.auth.client.UserProfileClient;
import com.boxcars.auth.dto.UserInfoResponse;
import com.boxcars.auth.entity.AuthProvider;
import com.boxcars.auth.entity.Role;
import com.boxcars.auth.entity.User;
import com.boxcars.auth.exception.ApiException;
import com.boxcars.auth.repository.RoleRepository;
import com.boxcars.auth.repository.UserRepository;
import com.boxcars.common.dto.AuthResponse;
import com.boxcars.common.dto.LoginRequest;
import com.boxcars.common.dto.RegisterRequest;
import com.boxcars.common.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_ROLE = "ROLE_RIDER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserProfileClient userProfileClient;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!StringUtils.hasText(request.username())) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Username is required");
        }
        if (!StringUtils.hasText(request.email())) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Email is required");
        }
        if (!StringUtils.hasText(request.password())) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Password is required");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT.value(), "Email is already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(HttpStatus.CONFLICT.value(), "Username is already taken");
        }

        Set<Role> roles = resolveRoles(request.roles());

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user.setProvider(AuthProvider.LOCAL);
        user.setRoles(roles);

        User saved = userRepository.save(user);
        List<String> roles = roleNames(saved);
        userProfileClient.syncProfile(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getPhone(), roles);
        return toAuthResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        if (!StringUtils.hasText(request.email()) || !StringUtils.hasText(request.password())) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Email and password are required");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password"));

        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN.value(), "Account is inactive");
        }
        if (user.getProvider() != AuthProvider.LOCAL || !StringUtils.hasText(user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Password login is not available for this account");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password");
        }

        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String token) {
        String jwt = extractToken(token);
        if (!jwtUtil.isValid(jwt)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired token");
        }

        Long userId = jwtUtil.getUserId(jwt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED.value(), "User not found for token"));

        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN.value(), "Account is inactive");
        }

        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserInfoResponse me(String authorizationHeader) {
        String jwt = extractBearerToken(authorizationHeader);
        if (!jwtUtil.isValid(jwt)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired token");
        }

        Long userId = jwtUtil.getUserId(jwt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "User not found"));

        return new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.isActive(),
                user.getProvider().name(),
                roleNames(user)
        );
    }

    private Set<Role> resolveRoles(List<String> requestedRoles) {
        List<String> roleNames = (requestedRoles == null || requestedRoles.isEmpty())
                ? List.of(DEFAULT_ROLE)
                : requestedRoles;

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            if (!StringUtils.hasText(roleName)) {
                continue;
            }
            String normalized = roleName.trim().toUpperCase();
            if (!normalized.startsWith("ROLE_")) {
                normalized = "ROLE_" + normalized;
            }
            Role role = roleRepository.findByName(normalized)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.BAD_REQUEST.value(),
                            "Unknown role: " + roleName
                    ));
            roles.add(role);
        }

        if (roles.isEmpty()) {
            Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Default role not seeded"
                    ));
            roles.add(defaultRole);
        }

        return roles;
    }

    private AuthResponse toAuthResponse(User user) {
        List<String> roles = roleNames(user);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), roles);
        return new AuthResponse(token, user.getId(), user.getEmail(), roles);
    }

    private List<String> roleNames(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .sorted()
                .toList();
    }

    private String extractBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED.value(), "Missing or invalid Authorization header");
        }
        return authorizationHeader.substring(7).trim();
    }

    private String extractToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Token is required");
        }
        String value = token.trim();
        if (value.startsWith("Bearer ")) {
            value = value.substring(7).trim();
        }
        if (!StringUtils.hasText(value)) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Token is required");
        }
        return value;
    }
}
