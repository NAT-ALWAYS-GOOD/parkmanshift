package com.parkmanshift.backend.infrastructure.web;

import com.parkmanshift.backend.application.port.in.LogMessageUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HelloControllerTest {

    @Mock
    private LogMessageUseCase logMessageUseCase;

    @InjectMocks
    private HelloController helloController;

    @Test
    public void testHelloEndpointDelegatesToUseCase() {
        when(logMessageUseCase.logAndSendMessage("Walking Skeleton")).thenReturn("Success message");

        String response = helloController.hello();

        assertEquals("Success message", response);
        verify(logMessageUseCase).logAndSendMessage("Walking Skeleton");
    }
}
