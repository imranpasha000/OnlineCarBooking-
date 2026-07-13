package com.boxcars.vehicle.controller;

import com.boxcars.vehicle.dto.CreateVehicleRequest;
import com.boxcars.vehicle.dto.UpdateStatusRequest;
import com.boxcars.vehicle.dto.UpdateVehicleRequest;
import com.boxcars.vehicle.entity.Vehicle;
import com.boxcars.vehicle.entity.VehicleStatus;
import com.boxcars.vehicle.entity.VehicleType;
import com.boxcars.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicle create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateVehicleRequest request
    ) {
        return vehicleService.create(userId, request);
    }

    @GetMapping
    public List<Vehicle> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestParam(required = false) Long ownerId
    ) {
        return vehicleService.list(userId, parseRoles(rolesHeader), ownerId);
    }

    @GetMapping("/search")
    public List<Vehicle> search(
            @RequestParam(required = false) VehicleType type,
            @RequestParam(required = false) VehicleStatus status
    ) {
        return vehicleService.search(type, status);
    }

    @GetMapping("/available")
    public List<Vehicle> available(@RequestParam(required = false) VehicleType type) {
        return vehicleService.available(type);
    }

    @GetMapping("/{id}")
    public Vehicle getById(@PathVariable Long id) {
        return vehicleService.getById(id);
    }

    @PutMapping("/{id}")
    public Vehicle update(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestBody UpdateVehicleRequest request
    ) {
        return vehicleService.update(id, userId, parseRoles(rolesHeader), request);
    }

    @PatchMapping("/{id}/status")
    public Vehicle updateStatus(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        return vehicleService.updateStatus(id, userId, parseRoles(rolesHeader), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader
    ) {
        vehicleService.delete(id, userId, parseRoles(rolesHeader));
    }

    private Set<String> parseRoles(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
