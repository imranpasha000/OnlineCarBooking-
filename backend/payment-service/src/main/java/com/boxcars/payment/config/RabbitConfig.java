package com.boxcars.payment.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.boxcars.common.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "boxcars.events";
    public static final String QUEUE = "payment.events";

    @Bean
    public TopicExchange boxcarsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentEventsQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding tripCompletedBinding(Queue paymentEventsQueue, TopicExchange boxcarsExchange) {
        return BindingBuilder.bind(paymentEventsQueue).to(boxcarsExchange).with(DomainEvent.TRIP_COMPLETED);
    }

    @Bean
    public Binding rentalReturnedBinding(Queue paymentEventsQueue, TopicExchange boxcarsExchange) {
        return BindingBuilder.bind(paymentEventsQueue).to(boxcarsExchange).with(DomainEvent.RENTAL_RETURNED);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.createXmlMapper(false).build();
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(mapper);
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("*");
        classMapper.setDefaultType(DomainEvent.class);
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setExchange(EXCHANGE);
        return template;
    }
}
