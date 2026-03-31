package com.parkmanshift.backend.application.service;

import com.parkmanshift.backend.application.port.in.GetReservationHistoryUseCase;
import com.parkmanshift.backend.application.port.in.ManageReservationUseCase;
import com.parkmanshift.backend.application.port.in.ReserveSpotUseCase;
import com.parkmanshift.backend.application.port.in.ViewParkingStateUseCase;
import com.parkmanshift.backend.application.port.out.MessageProducerPort;
import com.parkmanshift.backend.application.port.out.ParkingSpotRepositoryPort;
import com.parkmanshift.backend.application.port.out.ReservationRepositoryPort;
import com.parkmanshift.backend.domain.exception.ReservationLimitExceededException;
import com.parkmanshift.backend.domain.exception.ReservationNotFoundException;
import com.parkmanshift.backend.domain.exception.SpotNotAvailableException;
import com.parkmanshift.backend.domain.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ParkingService implements ReserveSpotUseCase, ManageReservationUseCase, ViewParkingStateUseCase, GetReservationHistoryUseCase {

    private final ParkingSpotRepositoryPort spotRepository;
    private final ReservationRepositoryPort reservationRepository;
    private final MessageProducerPort messageProducer;

    public ParkingService(ParkingSpotRepositoryPort spotRepository, ReservationRepositoryPort reservationRepository, MessageProducerPort messageProducer) {
        this.spotRepository = spotRepository;
        this.reservationRepository = reservationRepository;
        this.messageProducer = messageProducer;
    }

    @Override
    public Reservation reserveSpot(String parkingSpotLabel, String employeeId, UserRole userRole, LocalDate date) {
        int limit = (userRole == UserRole.MANAGER) ? 30 : 5;
        LocalDate today = LocalDate.now();

        List<Reservation> activeReservations = reservationRepository.findByEmployeeIdAndDateGreaterThanEqualAndStatusIn(
                employeeId, today, List.of(ReservationStatus.RESERVED, ReservationStatus.OCCUPIED)
        );
        if (activeReservations.size() >= limit) {
            throw new ReservationLimitExceededException("Maximum " + limit + " active/future reservations allowed.");
        }

        // Check if spot is free
        List<Reservation> spotReservations = reservationRepository.findByParkingSpotLabelAndDateAndStatusIn(
                parkingSpotLabel, date, List.of(ReservationStatus.RESERVED, ReservationStatus.OCCUPIED)
        );
        if (!spotReservations.isEmpty()) {
            throw new SpotNotAvailableException("Spot is already reserved or occupied on this date.");
        }

        Reservation res = new Reservation(UUID.randomUUID(), parkingSpotLabel, employeeId, date, ReservationStatus.RESERVED);
        Reservation saved = reservationRepository.save(res);

        messageProducer.sendEvent("{\"type\": \"ReservationCreated\", \"id\": \"" + saved.getId() + "\"}");

        return saved;
    }

    @Override
    public void cancelReservation(UUID reservationId, String employeeId, UserRole userRole) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        if (userRole != UserRole.SECRETARY && !res.getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("Not your reservation");
        }

        res.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(res);
    }

    @Override
    public void checkIn(UUID reservationId, String employeeId, UserRole userRole) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        if (userRole != UserRole.SECRETARY && !res.getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("Not your reservation");
        }

        if (res.getStatus() != ReservationStatus.RESERVED) {
            throw new SpotNotAvailableException("Cannot check in. Current status: " + res.getStatus());
        }

        res.setStatus(ReservationStatus.OCCUPIED);
        reservationRepository.save(res);
    }

    @Override
    public List<SpotState> getParkingStateForDate(LocalDate date) {
        List<ParkingSpot> spots = spotRepository.findAll();
        List<Reservation> reservationsForDate = reservationRepository.findByDate(date).stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .collect(Collectors.toList());

        List<SpotState> states = new ArrayList<>();
        for (ParkingSpot spot : spots) {
            Optional<Reservation> resOpt = reservationsForDate.stream()
                    .filter(r -> r.getParkingSpotLabel().equals(spot.getLabel()))
                    .findFirst();

            SpotStatus status = SpotStatus.FREE;
            if (resOpt.isPresent()) {
                if (resOpt.get().getStatus() == ReservationStatus.RESERVED) {
                    status = SpotStatus.RESERVED;
                } else if (resOpt.get().getStatus() == ReservationStatus.OCCUPIED) {
                    status = SpotStatus.OCCUPIED;
                }
            }
            states.add(new SpotState(spot, status));
        }

        return states;
    }

    @Override
    public List<Reservation> getHistory(String targetEmployeeId, String requesterId, UserRole requesterRole) {
        if (requesterRole != UserRole.SECRETARY && requesterRole != UserRole.MANAGER && !targetEmployeeId.equals(requesterId)) {
            throw new IllegalArgumentException("Access denied to this history");
        }
        return reservationRepository.findByEmployeeId(targetEmployeeId).stream()
                .sorted((r1, r2) -> r2.getDate().compareTo(r1.getDate()))
                .collect(Collectors.toList());
    }
}
