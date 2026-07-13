package com.boxcars.user.controller;

import com.boxcars.user.dto.DriverOnlineRequest;
import com.boxcars.user.dto.KycUpdateRequest;
import com.boxcars.user.dto.SyncUserRequest;
import com.boxcars.user.dto.UpdateProfileRequest;
import com.boxcars.user.entity.UserProfile;
import com.boxcars.user.exception.ForbiddenException;
import com.boxcars.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.OK)
    public UserProfile sync(@Valid @RequestBody SyncUserRequest request) {
        return userProfileService.sync(request);
    }

    @GetMapping("/me")
    public UserProfile me(@RequestHeader("X-User-Id") Long userId) {
        return userProfileService.getById(userId);
    }

    @PutMapping("/me")
    public UserProfile updateMe(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UpdateProfileRequest request
    ) {
        return userProfileService.updateMe(userId, request);
    }

    @PutMapping("/me/driver/online")
    public UserProfile updateDriverOnline(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody DriverOnlineRequest request
    ) {
        return userProfileService.updateDriverOnline(userId, request);
    }

    @GetMapping("/drivers/online")
    public List<UserProfile> onlineDrivers() {
        return userProfileService.listOnlineDrivers();
    }

    @GetMapping("/{id}")
    public UserProfile getById(@PathVariable Long id) {
        return userProfileService.getById(id);
    }

    @PutMapping("/{id}/kyc")
    public UserProfile updateKyc(
            @PathVariable Long id,
            @RequestHeader("X-User-Roles") String rolesHeader,
            @Valid @RequestBody KycUpdateRequest request
    ) {
        requireAdmin(rolesHeader);
        return userProfileService.updateKyc(id, request);
    }

    @GetMapping
    public List<UserProfile> listAll(@RequestHeader("X-User-Roles") String rolesHeader) {
        requireAdmin(rolesHeader);
        return userProfileService.listAll();
    }

    private void requireAdmin(String rolesHeader) {
        Set<String> roles = parseRoles(rolesHeader);
        if (!roles.contains("ROLE_ADMIN")) {
            throw new ForbiddenException("ROLE_ADMIN required");
        }
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
