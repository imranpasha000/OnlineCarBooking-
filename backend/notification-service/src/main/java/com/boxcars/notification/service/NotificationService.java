package com.boxcars.notification.service;

import com.boxcars.common.event.DomainEvent;
import com.boxcars.notification.model.NotificationRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {

    private final List<NotificationRecord> notifications = new CopyOnWriteArrayList<>();

    public void handleEvent(DomainEvent event) {
        if (event == null || event.type() == null) {
            return;
        }
        Map<String, Object> payload = event.payload() != null ? event.payload() : Map.of();
        List<NotificationDraft> drafts = mapEvent(event.type(), event.aggregateId(), payload);
        for (NotificationDraft draft : drafts) {
            NotificationRecord record = NotificationRecord.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(draft.userId())
                    .title(draft.title())
                    .body(draft.body())
                    .read(false)
                    .createdAt(Instant.now())
                    .build();
            notifications.add(record);
            log.info("Notification userId={} title={} body={}", record.getUserId(), record.getTitle(), record.getBody());
        }
    }

    public List<NotificationRecord> listForUser(Long userId) {
        return notifications.stream()
                .filter(n -> userId.equals(n.getUserId()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public int markAllRead(Long userId) {
        int count = 0;
        for (NotificationRecord record : notifications) {
            if (userId.equals(record.getUserId()) && !record.isRead()) {
                record.setRead(true);
                count++;
            }
        }
        return count;
    }

    private List<NotificationDraft> mapEvent(String type, String aggregateId, Map<String, Object> payload) {
        return switch (type) {
            case DomainEvent.TRIP_REQUESTED -> listOf(
                    draft(firstLong(payload, "riderId", "customerId"),
                            "Trip requested",
                            "Your trip request " + idLabel(aggregateId, payload, "tripId") + " is looking for a driver.")
            );
            case DomainEvent.TRIP_ASSIGNED -> listOf(
                    draft(firstLong(payload, "riderId", "customerId"),
                            "Driver assigned",
                            "A driver has been assigned to trip " + idLabel(aggregateId, payload, "tripId") + "."),
                    draft(firstLong(payload, "driverId"),
                            "New trip assigned",
                            "You have been assigned to trip " + idLabel(aggregateId, payload, "tripId") + ".")
            );
            case DomainEvent.TRIP_STARTED -> listOf(
                    draft(firstLong(payload, "riderId", "customerId"),
                            "Trip started",
                            "Your trip " + idLabel(aggregateId, payload, "tripId") + " has started."),
                    draft(firstLong(payload, "driverId"),
                            "Trip started",
                            "Trip " + idLabel(aggregateId, payload, "tripId") + " is underway.")
            );
            case DomainEvent.TRIP_COMPLETED -> listOf(
                    draft(firstLong(payload, "riderId", "customerId"),
                            "Trip completed",
                            "Your trip " + idLabel(aggregateId, payload, "tripId") + " is complete."),
                    draft(firstLong(payload, "driverId"),
                            "Trip completed",
                            "Trip " + idLabel(aggregateId, payload, "tripId") + " marked complete.")
            );
            case DomainEvent.TRIP_CANCELLED -> listOf(
                    draft(firstLong(payload, "riderId", "customerId"),
                            "Trip cancelled",
                            "Trip " + idLabel(aggregateId, payload, "tripId") + " was cancelled."),
                    draft(firstLong(payload, "driverId"),
                            "Trip cancelled",
                            "Trip " + idLabel(aggregateId, payload, "tripId") + " was cancelled.")
            );
            case DomainEvent.RENTAL_REQUESTED -> listOf(
                    draft(firstLong(payload, "ownerId"),
                            "Rental requested",
                            "A customer requested rental " + idLabel(aggregateId, payload, "rentalId") + "."),
                    draft(firstLong(payload, "customerId", "riderId"),
                            "Rental requested",
                            "Your rental request " + idLabel(aggregateId, payload, "rentalId") + " was submitted.")
            );
            case DomainEvent.RENTAL_CONFIRMED -> listOf(
                    draft(firstLong(payload, "customerId", "riderId"),
                            "Rental confirmed",
                            "Rental " + idLabel(aggregateId, payload, "rentalId") + " is confirmed."),
                    draft(firstLong(payload, "ownerId"),
                            "Rental confirmed",
                            "You confirmed rental " + idLabel(aggregateId, payload, "rentalId") + ".")
            );
            case DomainEvent.RENTAL_PICKED_UP -> listOf(
                    draft(firstLong(payload, "customerId", "riderId"),
                            "Vehicle picked up",
                            "Rental " + idLabel(aggregateId, payload, "rentalId") + " pickup recorded."),
                    draft(firstLong(payload, "ownerId"),
                            "Vehicle picked up",
                            "Rental " + idLabel(aggregateId, payload, "rentalId") + " was picked up.")
            );
            case DomainEvent.RENTAL_RETURNED -> listOf(
                    draft(firstLong(payload, "customerId", "riderId"),
                            "Rental returned",
                            "Rental " + idLabel(aggregateId, payload, "rentalId") + " has been returned."),
                    draft(firstLong(payload, "ownerId"),
                            "Rental returned",
                            "Your vehicle for rental " + idLabel(aggregateId, payload, "rentalId") + " was returned.")
            );
            case DomainEvent.PAYMENT_CAPTURED -> listOf(
                    draft(firstLong(payload, "payerId"),
                            "Payment captured",
                            "Payment " + idLabel(aggregateId, payload, "paymentId") + " was captured."),
                    draft(firstLong(payload, "payeeId"),
                            "Payment received",
                            "You received payment " + idLabel(aggregateId, payload, "paymentId") + ".")
            );
            case DomainEvent.DRIVER_OFFER -> {
                List<NotificationDraft> offers = new ArrayList<>();
                String tripLabel = idLabel(aggregateId, payload, "tripId");
                for (Long driverId : driverIdsFrom(payload)) {
                    offers.add(draft(driverId, "Trip offer", "New trip offer " + tripLabel + " is available."));
                }
                Long singleDriver = firstLong(payload, "driverId");
                if (singleDriver != null && offers.stream().noneMatch(d -> singleDriver.equals(d.userId()))) {
                    offers.add(draft(singleDriver, "Trip offer", "New trip offer " + tripLabel + " is available."));
                }
                Long riderId = firstLong(payload, "riderId", "customerId");
                if (riderId != null) {
                    offers.add(draft(riderId, "Looking for drivers",
                            "Matching drivers for trip " + tripLabel + "."));
                }
                yield listOf(offers.toArray(NotificationDraft[]::new));
            }
            default -> {
                log.debug("No notification mapping for event type={}", type);
                yield List.of();
            }
        };
    }

    private static List<NotificationDraft> listOf(NotificationDraft... drafts) {
        List<NotificationDraft> result = new ArrayList<>();
        for (NotificationDraft draft : drafts) {
            if (draft != null && draft.userId() != null) {
                result.add(draft);
            }
        }
        return result;
    }

    private static NotificationDraft draft(Long userId, String title, String body) {
        if (userId == null) {
            return null;
        }
        return new NotificationDraft(userId, title, body);
    }

    private static String idLabel(String aggregateId, Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return aggregateId != null ? aggregateId : "unknown";
    }

    private static Long firstLong(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Long value = toLong(payload.get(key));
            if (value != null) {
                return value;
            }
        }
        // DRIVER_OFFER may carry driverIds list — notify each
        return null;
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static List<Long> driverIdsFrom(Map<String, Object> payload) {
        Object raw = payload.get("driverIds");
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (Object item : list) {
            Long id = toLong(item);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private record NotificationDraft(Long userId, String title, String body) {
    }
}
