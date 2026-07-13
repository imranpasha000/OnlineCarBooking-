package com.boxcars.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record DomainEvent(
        String type,
        String aggregateId,
        Instant occurredAt,
        Map<String, Object> payload
) {
    public static DomainEvent of(String type, String aggregateId, Map<String, Object> payload) {
        return new DomainEvent(type, aggregateId, Instant.now(), payload);
    }

    public static final String TRIP_REQUESTED = "TRIP_REQUESTED";
    public static final String TRIP_ASSIGNED = "TRIP_ASSIGNED";
    public static final String TRIP_STARTED = "TRIP_STARTED";
    public static final String TRIP_COMPLETED = "TRIP_COMPLETED";
    public static final String TRIP_CANCELLED = "TRIP_CANCELLED";
    public static final String RENTAL_REQUESTED = "RENTAL_REQUESTED";
    public static final String RENTAL_CONFIRMED = "RENTAL_CONFIRMED";
    public static final String RENTAL_PICKED_UP = "RENTAL_PICKED_UP";
    public static final String RENTAL_RETURNED = "RENTAL_RETURNED";
    public static final String PAYMENT_CAPTURED = "PAYMENT_CAPTURED";
    public static final String DRIVER_OFFER = "DRIVER_OFFER";
}
