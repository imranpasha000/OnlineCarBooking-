package com.boxcars.matching.listener;

import com.boxcars.common.event.DomainEvent;
import com.boxcars.matching.config.RabbitConfig;
import com.boxcars.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingEventListener {

    private final MatchingService matchingService;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onDomainEvent(DomainEvent event) {
        if (event == null || event.type() == null) {
            return;
        }
        log.info("Matching service received event type={} aggregateId={}", event.type(), event.aggregateId());
        try {
            if (DomainEvent.TRIP_REQUESTED.equals(event.type())) {
                matchingService.handleTripRequested(event);
            }
        } catch (Exception ex) {
            log.error("Failed processing matching event: {}", ex.getMessage(), ex);
            // Ensure flow continues: publish empty DRIVER_OFFER on unexpected failure
            try {
                Long tripId = null;
                if (event.payload() != null && event.payload().get("tripId") != null) {
                    Object raw = event.payload().get("tripId");
                    if (raw instanceof Number number) {
                        tripId = number.longValue();
                    }
                }
                if (tripId == null && event.aggregateId() != null) {
                    tripId = Long.parseLong(event.aggregateId());
                }
                matchingService.publishDriverOffer(tripId, java.util.List.of(), event.payload());
            } catch (Exception nested) {
                log.error("Failed to publish fallback DRIVER_OFFER: {}", nested.getMessage());
            }
        }
    }
}
