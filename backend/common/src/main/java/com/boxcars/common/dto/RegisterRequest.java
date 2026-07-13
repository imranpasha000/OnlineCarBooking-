package com.boxcars.common.dto;

import java.util.List;

public record RegisterRequest(
        String username,
        String email,
        String phone,
        String password,
        List<String> roles
) {}
