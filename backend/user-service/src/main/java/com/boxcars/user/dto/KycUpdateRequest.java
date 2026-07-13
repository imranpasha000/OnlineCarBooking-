package com.boxcars.user.dto;

import jakarta.validation.constraints.NotNull;

public record KycUpdateRequest(
        @NotNull Boolean kycVerified
) {}
