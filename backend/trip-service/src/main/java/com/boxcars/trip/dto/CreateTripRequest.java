package com.boxcars.trip.dto;

import jakarta.validation.constraints.NotNull;

public record CreateTripRequest(
        @NotNull Double pickupLat,
        @NotNull Double pickupLng,
        String pickupAddress,
        @NotNull Double dropoffLat,
        @NotNull Double dropoffLng,
        String dropoffAddress,
        Long vehicleId
) {}
