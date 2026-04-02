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
    public ChangePasswordUseCase changePasswordUseCase(UserService userService) {
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
            UserRepositoryPort userRepository,
            MessageProducerPort messageProducer) {
        return new ParkingService(spotRepository, reservationRepository, userRepository, messageProducer);
    }

    @Bean
    public ReserveSpotUseCase reserveSpotUseCase(ParkingService parkingService) { return parkingService; }
    @Bean
    public ManageReservationUseCase manageReservationUseCase(ParkingService parkingService) { return parkingService; }
    @Bean
    public ViewParkingStateUseCase viewParkingStateUseCase(ParkingService parkingService) { return parkingService; }
    @Bean
    public GetReservationHistoryUseCase getReservationHistoryUseCase(ParkingService parkingService) { return parkingService; }
    @Bean
    public GetDashboardStatsUseCase getDashboardStatsUseCase(ParkingService parkingService) { return parkingService; }
    @Bean
    public CancelUnconfirmedReservationsUseCase cancelUnconfirmedReservationsUseCase(ParkingService parkingService) { return parkingService; }
    @Bean
    public UpdateReservationUseCase updateReservationUseCase(ParkingService parkingService) { return parkingService; }
    @Bean
    public CheckInWithCodeUseCase checkInWithCodeUseCase(ParkingService parkingService) { return parkingService; }
}
