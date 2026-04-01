package com.parkmanshift.backend.infrastructure.config;

import com.parkmanshift.backend.application.port.in.*;
import com.parkmanshift.backend.application.port.out.*;
import com.parkmanshift.backend.application.service.LogMessageService;
import com.parkmanshift.backend.application.service.ParkingService;
import com.parkmanshift.backend.application.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public UserService userService(
            UserRepositoryPort userRepository,
            PasswordEncoder passwordEncoder) {
        return new UserService(userRepository, passwordEncoder);
    }

    @Bean
    public CreateUserUseCase createUserUseCase(UserService userService) {
        return userService;
    }

    @Bean
    public SearchUserUseCase searchUserUseCase(UserService userService) {
        return userService;
    }

    @Bean
    public LogMessageUseCase logMessageUseCase(
            SkeletonLogRepositoryPort repository,
            MessageProducerPort messageProducer) {
        return new LogMessageService(repository, messageProducer);
    }

    @Bean
    public ParkingService parkingService(
            ParkingSpotRepositoryPort spotRepository,
            ReservationRepositoryPort reservationRepository,
            MessageProducerPort messageProducer) {
        return new ParkingService(spotRepository, reservationRepository, messageProducer);
    }
}
