package com.parkmanshift.backend.controller;

import com.parkmanshift.backend.HelloController;
import com.parkmanshift.backend.entity.SkeletonLog;
import com.parkmanshift.backend.messaging.MessageProducer;
import com.parkmanshift.backend.repository.SkeletonLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
public class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageProducer messageProducer;

    @MockBean
    private SkeletonLogRepository skeletonLogRepository;

    @Test
    public void testHelloEndpointReturnsMessageAndPublishesEventAndSavesToDb() throws Exception {
        when(skeletonLogRepository.count()).thenReturn(1L);

        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Connexion réussie ! API + RabbitMQ + DB connectés. Total des accès en DB : 1"));

        verify(messageProducer).sendReservationEvent("Test reservation message sent from Walking Skeleton endpoint. Total logs in DB: 1");
        
        verify(skeletonLogRepository).save(ArgumentMatchers.any(SkeletonLog.class));
    }
}
