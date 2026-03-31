package com.parkmanshift.backend.infrastructure.messaging;

import com.parkmanshift.backend.application.port.out.MessageProducerPort;
import com.parkmanshift.backend.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQMessageProducerAdapter implements MessageProducerPort {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQMessageProducerAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendEvent(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
    }
}
