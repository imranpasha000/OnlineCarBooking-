package com.boxcars.notification.listener;

import com.boxcars.common.event.DomainEvent;
import com.boxcars.notification.config.RabbitConfig;
import com.boxcars.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onDomainEvent(DomainEvent event) {
        try {
            notificationService.handleEvent(event);
        } catch (Exception ex) {
            log.error("Failed to process notification for event {}: {}",
                    event != null ? event.type() : null, ex.getMessage(), ex);
        }
    }
}
