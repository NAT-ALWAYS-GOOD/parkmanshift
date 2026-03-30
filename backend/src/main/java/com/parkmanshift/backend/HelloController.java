package com.parkmanshift.backend;

import com.parkmanshift.backend.entity.SkeletonLog;
import com.parkmanshift.backend.messaging.MessageProducer;
import com.parkmanshift.backend.repository.SkeletonLogRepository;

import jakarta.transaction.Transactional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    private final MessageProducer messageProducer;
    private final SkeletonLogRepository repository;

    public HelloController(MessageProducer messageProducer, SkeletonLogRepository repository) {
        this.messageProducer = messageProducer;
        this.repository = repository;
    }

    @GetMapping("/hello")
    @Transactional
    public String hello() {
        SkeletonLog log = new SkeletonLog("Walking Skeleton");
        repository.save(log);
        long count = repository.count();

        messageProducer.sendReservationEvent("Test reservation message sent from Walking Skeleton endpoint. Total logs in DB: " + count);

        return String.format("{\"message\": \"Connexion réussie ! API + RabbitMQ + DB connectés. Total des accès en DB : %d\"}", count);
    }
}
