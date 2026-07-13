package com.boxcars.matching.controller;

import com.boxcars.matching.dto.DriverLocationRequest;
import com.boxcars.matching.dto.NearbyDriverResponse;
import com.boxcars.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matching")
@CrossOrigin
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping("/drivers/location")
    public Map<String, Object> updateLocation(
            @RequestHeader("X-User-Id") Long driverId,
            @RequestBody DriverLocationRequest request
    ) {
        if (request.getLat() == null || request.getLng() == null) {
            throw new IllegalArgumentException("lat and lng are required");
        }
        matchingService.updateDriverLocation(driverId, request.getLat(), request.getLng());
        return Map.of("driverId", driverId, "status", "online");
    }

    @PostMapping("/drivers/offline")
    public Map<String, Object> offline(@RequestHeader("X-User-Id") Long driverId) {
        matchingService.setDriverOffline(driverId);
        return Map.of("driverId", driverId, "status", "offline");
    }

    @GetMapping("/drivers/nearby")
    public List<NearbyDriverResponse> nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radiusKm
    ) {
        return matchingService.findNearby(lat, lng, radiusKm);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "matching-service");
    }
}
