package com.boxcars.trip.service;

import com.boxcars.common.event.DomainEvent;
import com.boxcars.trip.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(DomainEvent event) {
        String routingKey = event.type().toLowerCase().replace('_', '.');
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
    }
}
