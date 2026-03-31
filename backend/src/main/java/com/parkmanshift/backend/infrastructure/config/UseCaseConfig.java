package com.parkmanshift.backend.infrastructure.config;

import com.parkmanshift.backend.application.port.in.LogMessageUseCase;
import com.parkmanshift.backend.application.port.out.MessageProducerPort;
import com.parkmanshift.backend.application.port.out.SkeletonLogRepositoryPort;
import com.parkmanshift.backend.application.service.LogMessageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public LogMessageUseCase logMessageUseCase(
            SkeletonLogRepositoryPort repository,
            MessageProducerPort messageProducer) {
        return new LogMessageService(repository, messageProducer);
    }
}
