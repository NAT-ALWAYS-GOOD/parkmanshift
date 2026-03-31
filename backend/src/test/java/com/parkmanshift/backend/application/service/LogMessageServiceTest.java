package com.parkmanshift.backend.application.service;

import com.parkmanshift.backend.application.port.out.MessageProducerPort;
import com.parkmanshift.backend.application.port.out.SkeletonLogRepositoryPort;
import com.parkmanshift.backend.domain.model.SkeletonLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LogMessageServiceTest {

    @Mock
    private SkeletonLogRepositoryPort repository;

    @Mock
    private MessageProducerPort messageProducer;

    @InjectMocks
    private LogMessageService logMessageService;

    @Test
    public void testLogAndSendMessage() {
        when(repository.count()).thenReturn(1L);

        String response = logMessageService.logAndSendMessage("Walking Skeleton");

        assertTrue(response.contains("Connexion réussie ! API + RabbitMQ + DB connectés. Total des accès en DB : 1"));
        verify(messageProducer).sendEvent("Test reservation message sent from Walking Skeleton endpoint. Total logs in DB: 1");
        verify(repository).save(ArgumentMatchers.any(SkeletonLog.class));
    }
}
