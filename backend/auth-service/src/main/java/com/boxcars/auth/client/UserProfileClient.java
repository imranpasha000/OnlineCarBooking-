package com.boxcars.auth.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProfileClient {

    private final RestClient userServiceClient;

    public void syncProfile(Long userId, String username, String email, String phone, List<String> roles) {
        try {
            userServiceClient.post()
                    .uri("/api/users/sync")
                    .body(Map.of(
                            "userId", userId,
                            "username", username,
                            "email", email,
                            "phone", phone == null ? "" : phone,
                            "roles", roles
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to sync user profile for {}: {}", userId, e.getMessage());
        }
    }
}
