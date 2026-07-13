package com.boxcars.vehicle.dto;

import com.boxcars.vehicle.entity.VehicleStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull VehicleStatus status
) {}
