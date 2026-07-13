package com.boxcars.payment.listener;

import com.boxcars.common.event.DomainEvent;
import com.boxcars.payment.config.RabbitConfig;
import com.boxcars.payment.entity.ReferenceType;
import com.boxcars.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final PaymentService paymentService;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onDomainEvent(DomainEvent event) {
        if (event == null || event.type() == null) {
            return;
        }
        log.info("Payment service received event type={} aggregateId={}", event.type(), event.aggregateId());

        try {
            switch (event.type()) {
                case DomainEvent.TRIP_COMPLETED -> handleTripCompleted(event);
                case DomainEvent.RENTAL_RETURNED -> handleRentalReturned(event);
                default -> log.debug("Ignoring event type={}", event.type());
            }
        } catch (Exception ex) {
            log.error("Failed processing payment event type={}: {}", event.type(), ex.getMessage(), ex);
        }
    }

    private void handleTripCompleted(DomainEvent event) {
        Map<String, Object> payload = event.payload() != null ? event.payload() : Map.of();
        Long payerId = firstLong(payload, "riderId", "customerId");
        Long payeeId = firstLong(payload, "driverId", "ownerId");
        Long referenceId = firstLong(payload, "tripId");
        if (referenceId == null && event.aggregateId() != null) {
            referenceId = parseLong(event.aggregateId());
        }
        BigDecimal amount = toBigDecimal(payload.get("amount"));

        if (payerId == null || payeeId == null || referenceId == null || amount == null) {
            log.warn("TRIP_COMPLETED missing payment fields: payerId={} payeeId={} tripId={} amount={}",
                    payerId, payeeId, referenceId, amount);
            return;
        }

        paymentService.autoAuthorizeAndCapture(ReferenceType.TRIP, referenceId, payerId, payeeId, amount);
    }

    private void handleRentalReturned(DomainEvent event) {
        Map<String, Object> payload = event.payload() != null ? event.payload() : Map.of();
        Long payerId = firstLong(payload, "customerId", "riderId");
        Long payeeId = firstLong(payload, "ownerId", "driverId");
        Long referenceId = firstLong(payload, "rentalId");
        if (referenceId == null && event.aggregateId() != null) {
            referenceId = parseLong(event.aggregateId());
        }
        BigDecimal amount = toBigDecimal(payload.get("amount"));

        if (payerId == null || payeeId == null || referenceId == null || amount == null) {
            log.warn("RENTAL_RETURNED missing payment fields: payerId={} payeeId={} rentalId={} amount={}",
                    payerId, payeeId, referenceId, amount);
            return;
        }

        paymentService.autoAuthorizeAndCapture(ReferenceType.RENTAL, referenceId, payerId, payeeId, amount);
    }

    private static Long firstLong(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Long value = toLong(payload.get(key));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return parseLong(String.valueOf(value));
    }

    private static Long parseLong(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
