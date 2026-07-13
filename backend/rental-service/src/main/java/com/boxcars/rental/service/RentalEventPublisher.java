package com.boxcars.rental.service;

import com.boxcars.common.event.DomainEvent;
import com.boxcars.rental.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(DomainEvent event) {
        String routingKey = event.type().toLowerCase().replace('_', '.');
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
    }
}
