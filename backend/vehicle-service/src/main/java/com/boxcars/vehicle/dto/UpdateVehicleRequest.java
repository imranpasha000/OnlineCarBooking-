package com.boxcars.vehicle.dto;

import com.boxcars.vehicle.entity.VehicleType;

import java.math.BigDecimal;

public record UpdateVehicleRequest(
        String name,
        String brand,
        String model,
        Integer year,
        String plateNumber,
        VehicleType type,
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
