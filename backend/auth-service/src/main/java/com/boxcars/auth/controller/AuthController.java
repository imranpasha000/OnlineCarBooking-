package com.boxcars.auth.controller;

import com.boxcars.auth.dto.RefreshTokenRequest;
import com.boxcars.auth.dto.UserInfoResponse;
import com.boxcars.auth.service.AuthService;
import com.boxcars.common.dto.AuthResponse;
import com.boxcars.common.dto.LoginRequest;
import com.boxcars.common.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) RefreshTokenRequest body
    ) {
        String token = null;
        if (StringUtils.hasText(authorization)) {
            token = authorization;
        } else if (body != null && StringUtils.hasText(body.token())) {
            token = body.token();
        }
        return ResponseEntity.ok(authService.refresh(token));
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(authService.me(authorization));
    }
}
