package com.parkmanshift.backend.application.port.in;

public interface LogMessageUseCase {
    String logAndSendMessage(String messageContent);
}
