package com.boxcars.vehicle.dto;

import com.boxcars.vehicle.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateVehicleRequest(
        @NotBlank String name,
        String brand,
        String model,
        Integer year,
        String plateNumber,
        @NotNull VehicleType type,
        BigDecimal pricePerDay,
        BigDecimal pricePerKm,
        String imageUrl,
        String fuelType,
        String transmission,
        Integer seats,
        String description,
        Double lat,
        Double lng
) {}
