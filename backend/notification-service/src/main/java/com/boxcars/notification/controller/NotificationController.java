package com.boxcars.notification.controller;

import com.boxcars.notification.model.NotificationRecord;
import com.boxcars.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public List<NotificationRecord> me(@RequestHeader("X-User-Id") Long userId) {
        return notificationService.listForUser(userId);
    }

    @PostMapping("/me/read-all")
    public Map<String, Object> readAll(@RequestHeader("X-User-Id") Long userId) {
        int updated = notificationService.markAllRead(userId);
        return Map.of("updated", updated);
    }
}
