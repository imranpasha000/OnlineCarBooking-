package com.boxcars.rental.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookRentalRequest(
        @NotNull Long vehicleId,
        @NotNull Long ownerId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String pickupAddress,
        String returnAddress,
        @NotNull BigDecimal dailyRate
) {}
