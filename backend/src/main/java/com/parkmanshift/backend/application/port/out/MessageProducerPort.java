package com.parkmanshift.backend.application.port.out;

public interface MessageProducerPort {
    void sendEvent(String message);
}
