package com.parkmanshift.backend.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "parkmanshift-exchange";
    public static final String QUEUE_NAME = "reservation-emails-queue";
    public static final String ROUTING_KEY = "reservation.created";

    @Bean
    public Queue reservationQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue reservationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(reservationQueue).to(exchange).with(ROUTING_KEY);
    }
}
