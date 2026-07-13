package com.boxcars.user.dto;

import jakarta.validation.constraints.NotNull;

public record DriverOnlineRequest(
        @NotNull Boolean online,
        Double lat,
        Double lng
) {}
