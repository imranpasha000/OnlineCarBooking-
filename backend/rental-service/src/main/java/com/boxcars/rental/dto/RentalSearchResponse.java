package com.boxcars.rental.dto;

import java.util.List;

/**
 * MVP search: clients should load rental vehicles from vehicle-service
 * {@code GET /api/vehicles/search?type=RENTAL}, then exclude {@code unavailableVehicleIds}
 * that already have overlapping REQUESTED/CONFIRMED/PICKED_UP bookings.
 */
public record RentalSearchResponse(
        String message,
        List<Long> unavailableVehicleIds
) {}
