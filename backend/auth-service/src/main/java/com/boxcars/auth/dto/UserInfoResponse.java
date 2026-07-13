package com.boxcars.auth.dto;

import java.util.List;

public record UserInfoResponse(
        Long id,
        String username,
        String email,
        String phone,
        boolean active,
        String provider,
        List<String> roles
) {}
