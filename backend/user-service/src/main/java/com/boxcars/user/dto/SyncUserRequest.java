package com.boxcars.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SyncUserRequest(
        @NotNull Long userId,
        @NotBlank String username,
        @NotBlank @Email String email,
        String phone,
        List<String> roles
) {}
