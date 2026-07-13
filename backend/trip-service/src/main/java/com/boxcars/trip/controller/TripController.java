package com.boxcars.trip.controller;

import com.boxcars.trip.dto.CreateTripRequest;
import com.boxcars.trip.dto.LocationUpdateRequest;
import com.boxcars.trip.entity.Trip;
import com.boxcars.trip.exception.ResourceNotFoundException;
import com.boxcars.trip.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Trip create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateTripRequest request
    ) {
        return tripService.create(userId, request);
    }

    @GetMapping("/me")
    public List<Trip> myTrips(@RequestHeader("X-User-Id") Long userId) {
        return tripService.myTrips(userId);
    }

    @GetMapping("/pending")
    public List<Trip> pending() {
        return tripService.pending();
    }

    @GetMapping("/active")
    public Trip active(@RequestHeader("X-User-Id") Long userId) {
        return tripService.active(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active trip for user " + userId));
    }

    @GetMapping("/{id}")
    public Trip getById(@PathVariable Long id) {
        return tripService.getById(id);
    }

    @PostMapping("/{id}/accept")
    public Trip accept(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long driverId
    ) {
        return tripService.accept(id, driverId);
    }

    @PostMapping("/{id}/start")
    public Trip start(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long driverId
    ) {
        return tripService.start(id, driverId);
    }

    @PostMapping("/{id}/complete")
    public Trip complete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long driverId
    ) {
        return tripService.complete(id, driverId);
    }

    @PostMapping("/{id}/cancel")
    public Trip cancel(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return tripService.cancel(id, userId);
    }

    @PostMapping("/{id}/location")
    public Map<String, Double> updateLocation(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long driverId,
            @Valid @RequestBody LocationUpdateRequest request
    ) {
        return tripService.updateLocation(id, driverId, request);
    }
}
