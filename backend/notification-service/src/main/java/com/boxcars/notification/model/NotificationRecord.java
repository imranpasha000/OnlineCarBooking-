package com.boxcars.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRecord {

    private String id;
    private Long userId;
    private String title;
    private String body;
    private boolean read;
    private Instant createdAt;
}
