package com.parkmanshift.backend.application.service;

import com.parkmanshift.backend.application.port.in.LogMessageUseCase;
import com.parkmanshift.backend.application.port.out.MessageProducerPort;
import com.parkmanshift.backend.application.port.out.SkeletonLogRepositoryPort;
import com.parkmanshift.backend.domain.model.SkeletonLog;

public class LogMessageService implements LogMessageUseCase {

    private final SkeletonLogRepositoryPort repository;
    private final MessageProducerPort messageProducer;

    public LogMessageService(SkeletonLogRepositoryPort repository, MessageProducerPort messageProducer) {
        this.repository = repository;
        this.messageProducer = messageProducer;
    }

    @Override
    public String logAndSendMessage(String messageContent) {
        SkeletonLog log = new SkeletonLog(messageContent);
        repository.save(log);
        long count = repository.count();

        messageProducer.sendEvent("Test reservation message sent from Walking Skeleton endpoint. Total logs in DB: " + count);

        return String.format("{\"message\": \"Connexion réussie ! API + RabbitMQ + DB connectés. Total des accès en DB : %d\"}", count);
    }
}
