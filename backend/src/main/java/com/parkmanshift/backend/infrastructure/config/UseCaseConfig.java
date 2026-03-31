package com.parkmanshift.backend.infrastructure.config;

import com.parkmanshift.backend.application.port.in.LogMessageUseCase;
import com.parkmanshift.backend.application.port.in.ManageReservationUseCase;
import com.parkmanshift.backend.application.port.in.ReserveSpotUseCase;
import com.parkmanshift.backend.application.port.in.ViewParkingStateUseCase;
import com.parkmanshift.backend.application.port.out.MessageProducerPort;
import com.parkmanshift.backend.application.port.out.ParkingSpotRepositoryPort;
import com.parkmanshift.backend.application.port.out.ReservationRepositoryPort;
import com.parkmanshift.backend.application.port.out.SkeletonLogRepositoryPort;
import com.parkmanshift.backend.application.service.LogMessageService;
import com.parkmanshift.backend.application.service.ParkingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

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

    @Bean
    public ReserveSpotUseCase reserveSpotUseCase(ParkingService parkingService) {
        return parkingService;
    }

    @Bean
    public ManageReservationUseCase manageReservationUseCase(ParkingService parkingService) {
        return parkingService;
    }

    @Bean
    public ViewParkingStateUseCase viewParkingStateUseCase(ParkingService parkingService) {
        return parkingService;
    }
}
