package com.parkmanshift.backend.infrastructure.web;

import com.parkmanshift.backend.application.port.in.LogMessageUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    private final LogMessageUseCase logMessageUseCase;

    public HelloController(LogMessageUseCase logMessageUseCase) {
        this.logMessageUseCase = logMessageUseCase;
    }

    @GetMapping("/hello")
    public String hello() {
        return logMessageUseCase.logAndSendMessage("Walking Skeleton");
    }
}
