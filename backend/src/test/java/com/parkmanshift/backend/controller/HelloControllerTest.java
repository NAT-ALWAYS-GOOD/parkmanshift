package com.parkmanshift.backend.controller;

import com.parkmanshift.backend.HelloController;
import com.parkmanshift.backend.entity.SkeletonLog;
import com.parkmanshift.backend.messaging.MessageProducer;
import com.parkmanshift.backend.repository.SkeletonLogRepository;
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
public class HelloControllerTest {

    @Mock
    private MessageProducer messageProducer;

    @Mock
    private SkeletonLogRepository skeletonLogRepository;

    @InjectMocks
    private HelloController helloController;

    @Test
    public void testHelloEndpointReturnsMessageAndPublishesEventAndSavesToDb() {
        // Arrange : simulation du retour de la DB
        when(skeletonLogRepository.count()).thenReturn(1L);

        // Act : appel direct du POJO contrôleur
        String response = helloController.hello();

        // Assert : vérification de la réponse texte
        assertTrue(response.contains("Connexion réussie ! API + RabbitMQ + DB connectés. Total des accès en DB : 1"));

        // Vérifie qu'un message d'intégration asynchrone a bien été envoyé à RabbitMQ
        verify(messageProducer).sendReservationEvent("Test reservation message sent from Walking Skeleton endpoint. Total logs in DB: 1");
        
        // Vérifie qu'une commande d'insertion de log a bien été demandée à Postgres
        verify(skeletonLogRepository).save(ArgumentMatchers.any(SkeletonLog.class));
    }
}
