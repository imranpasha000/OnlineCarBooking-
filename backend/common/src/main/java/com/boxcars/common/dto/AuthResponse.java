package com.boxcars.common.dto;

public record AuthResponse(String accessToken, String tokenType, Long userId, String email, java.util.List<String> roles) {
    public AuthResponse(String accessToken, Long userId, String email, java.util.List<String> roles) {
        this(accessToken, "Bearer", userId, email, roles);
    }
}
