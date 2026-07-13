package com.boxcars.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyDriverResponse {
    private Long driverId;
    private Double distanceKm;
    private Double lat;
    private Double lng;
}
