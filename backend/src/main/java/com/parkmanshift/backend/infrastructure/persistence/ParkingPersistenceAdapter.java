package com.parkmanshift.backend.infrastructure.persistence;

import com.parkmanshift.backend.application.port.out.ParkingSpotRepositoryPort;
import com.parkmanshift.backend.application.port.out.ReservationRepositoryPort;
import com.parkmanshift.backend.domain.model.ParkingSpot;
import com.parkmanshift.backend.domain.model.Reservation;
import com.parkmanshift.backend.domain.model.ReservationStatus;
import com.parkmanshift.backend.domain.model.SpotType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ParkingPersistenceAdapter implements ParkingSpotRepositoryPort, ReservationRepositoryPort {

    private final SpringDataParkingSpotRepository spotRepository;
    private final SpringDataReservationRepository reservationRepository;

    public ParkingPersistenceAdapter(SpringDataParkingSpotRepository spotRepository, SpringDataReservationRepository reservationRepository) {
        this.spotRepository = spotRepository;
        this.reservationRepository = reservationRepository;
    }

    // --- ParkingSpotRepositoryPort ---

    @Override
    public List<ParkingSpot> findAll() {
        return spotRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ParkingSpot> findByLabel(String label) {
        return spotRepository.findById(label).map(this::toDomain);
    }

    @Override
    public void saveAll(List<ParkingSpot> spots) {
        List<ParkingSpotEntity> entities = spots.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        spotRepository.saveAll(entities);
    }

    // --- ReservationRepositoryPort ---

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity entity = new ReservationEntity(
                reservation.getId(),
                reservation.getParkingSpotLabel(),
                reservation.getEmployeeId(),
                reservation.getDate(),
                reservation.getStatus().name()
        );
        ReservationEntity saved = reservationRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return reservationRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Reservation> findByDate(LocalDate date) {
        return reservationRepository.findByReservationDate(date).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByEmployeeIdAndStatusIn(String employeeId, List<ReservationStatus> statuses) {
        List<String> statusStrings = statuses.stream().map(Enum::name).collect(Collectors.toList());
        return reservationRepository.findByEmployeeIdAndStatusIn(employeeId, statusStrings).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByParkingSpotLabelAndDateAndStatusIn(String label, LocalDate date, List<ReservationStatus> statuses) {
        List<String> statusStrings = statuses.stream().map(Enum::name).collect(Collectors.toList());
        return reservationRepository.findByParkingSpotLabelAndReservationDateAndStatusIn(label, date, statusStrings).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // Mappers

    private ParkingSpot toDomain(ParkingSpotEntity entity) {
        return new ParkingSpot(entity.getLabel(), SpotType.valueOf(entity.getType()));
    }

    private ParkingSpotEntity toEntity(ParkingSpot domain) {
        return new ParkingSpotEntity(domain.getLabel(), domain.getType().name());
    }

    private Reservation toDomain(ReservationEntity entity) {
        return new Reservation(
                entity.getId(),
                entity.getParkingSpotLabel(),
                entity.getEmployeeId(),
                entity.getReservationDate(),
                ReservationStatus.valueOf(entity.getStatus())
        );
    }
}
