package com.parkmanshift.backend.application.service;

import com.parkmanshift.backend.application.port.in.CancelUnconfirmedReservationsUseCase;
import com.parkmanshift.backend.application.port.in.GetDashboardStatsUseCase;
import com.parkmanshift.backend.application.port.in.GetReservationHistoryUseCase;
import com.parkmanshift.backend.application.port.in.ManageReservationUseCase;
import com.parkmanshift.backend.application.port.in.ReserveSpotUseCase;
import com.parkmanshift.backend.application.port.in.UpdateReservationUseCase;
import com.parkmanshift.backend.application.port.in.ViewParkingStateUseCase;
import com.parkmanshift.backend.application.port.out.MessageProducerPort;
import com.parkmanshift.backend.application.port.out.ParkingSpotRepositoryPort;
import com.parkmanshift.backend.application.port.out.ReservationRepositoryPort;
import com.parkmanshift.backend.domain.exception.ReservationLimitExceededException;
import com.parkmanshift.backend.domain.exception.ReservationNotFoundException;
import com.parkmanshift.backend.domain.exception.SpotNotAvailableException;
import com.parkmanshift.backend.domain.exception.UserAlreadyReservedException;
import com.parkmanshift.backend.domain.model.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ParkingService implements ReserveSpotUseCase, ManageReservationUseCase, ViewParkingStateUseCase, GetReservationHistoryUseCase, GetDashboardStatsUseCase, CancelUnconfirmedReservationsUseCase, UpdateReservationUseCase {

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
        LocalDate today = LocalDate.now();

        // Secretaries bypass the reservation limit when booking for others
        if (userRole != UserRole.SECRETARY) {
            int limit = (userRole == UserRole.MANAGER) ? 30 : 5;
            List<Reservation> activeReservations = reservationRepository.findByEmployeeIdAndDateGreaterThanEqualAndStatusIn(
                    employeeId, today, List.of(ReservationStatus.RESERVED, ReservationStatus.OCCUPIED)
            );
            if (activeReservations.size() >= limit) {
                throw new ReservationLimitExceededException("Maximum " + limit + " active/future reservations allowed.");
            }
        }

        // Check if employee already has a reservation on this day
        List<Reservation> sameDayReservations = reservationRepository.findByEmployeeIdAndDateAndStatusIn(
                employeeId, date, List.of(ReservationStatus.RESERVED, ReservationStatus.OCCUPIED)
        );
        if (!sameDayReservations.isEmpty()) {
            throw new UserAlreadyReservedException("User already has a reservation for this day.");
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
            String reservedBy = null;
            ReservationStatus resStatus = null;
            java.util.UUID resId = null;
            if (resOpt.isPresent()) {
                Reservation r = resOpt.get();
                reservedBy = r.getEmployeeId();
                resStatus = r.getStatus();
                resId = r.getId();
                if (r.getStatus() == ReservationStatus.RESERVED) {
                    status = SpotStatus.RESERVED;
                } else if (r.getStatus() == ReservationStatus.OCCUPIED) {
                    status = SpotStatus.OCCUPIED;
                }
            }
            states.add(new SpotState(spot, status, reservedBy, resStatus, resId));
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

    @Override
    public DashboardStats getDashboardStats(UserRole requesterRole, YearMonth yearMonth, String employeeId) {
        if (requesterRole != UserRole.MANAGER && requesterRole != UserRole.SECRETARY) {
            throw new IllegalArgumentException("Access denied to dashboard");
        }

        List<Reservation> allReservations = reservationRepository.findAllReservations();
        List<ParkingSpot> allSpots = spotRepository.findAll();

        if (yearMonth != null) {
            allReservations = allReservations.stream()
                    .filter(r -> YearMonth.from(r.getDate()).equals(yearMonth))
                    .collect(Collectors.toList());
        }

        if (employeeId != null && !employeeId.isBlank()) {
            allReservations = allReservations.stream()
                    .filter(r -> r.getEmployeeId().equals(employeeId))
                    .collect(Collectors.toList());
        }

        int totalReservations = allReservations.size();
        
        long noShows = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CANCELLED)
                .count();
        double noShowProportion = totalReservations > 0 ? (double) noShows / totalReservations * 100 : 0.0;

        long occupied = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.OCCUPIED)
                .count();

        double occupancyRate = totalReservations > 0 ? (double) occupied / totalReservations * 100 : 0.0;

        long electricSpotsUsed = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.OCCUPIED || r.getStatus() == ReservationStatus.RESERVED)
                .filter(r -> {
                    return allSpots.stream()
                            .filter(s -> s.getLabel().equals(r.getParkingSpotLabel()))
                            .findFirst()
                            .map(s -> s.getType() == SpotType.ELECTRIC)
                            .orElse(false);
                })
                .count();
        
        long totalElectricSpots = allSpots.stream().filter(s -> s.getType() == SpotType.ELECTRIC).count();
        double electricSpotProportion = (totalElectricSpots > 0 && totalReservations > 0) ? (double) electricSpotsUsed / totalReservations * 100 : 0.0;

        return new DashboardStats(occupancyRate, noShowProportion, electricSpotProportion, totalReservations);
    }

    @Override
    public int cancelUnconfirmedReservationsForToday() {
        LocalDate today = LocalDate.now();
        List<Reservation> todayReservations = reservationRepository.findByDate(today);

        int cancelledCount = 0;
        for (Reservation res : todayReservations) {
            if (res.getStatus() == ReservationStatus.RESERVED) {
                res.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(res);
                messageProducer.sendEvent("{\"type\": \"ReservationAutoCancelled\", \"id\": \"" + res.getId() + "\"}");
                cancelledCount++;
            }
        }
        return cancelledCount;
    }

    @Override
    public Reservation updateReservation(UUID reservationId, String newSpotLabel, LocalDate newDate, String requesterId, UserRole requesterRole) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        if (requesterRole != UserRole.SECRETARY && !res.getEmployeeId().equals(requesterId)) {
            throw new IllegalArgumentException("Not your reservation");
        }

        if (res.getStatus() != ReservationStatus.RESERVED) {
            throw new SpotNotAvailableException("Cannot update. Current status: " + res.getStatus());
        }

        // Check availability on new (spot, date) excluding current reservation
        List<Reservation> spotReservations = reservationRepository.findByParkingSpotLabelAndDateAndStatusIn(
                newSpotLabel, newDate, List.of(ReservationStatus.RESERVED, ReservationStatus.OCCUPIED)
        );

        boolean isAlreadyTakenByOther = spotReservations.stream()
                .anyMatch(r -> !r.getId().equals(reservationId));

        if (isAlreadyTakenByOther) {
            throw new SpotNotAvailableException("The new spot is already reserved or occupied on this date.");
        }

        res.setParkingSpotLabel(newSpotLabel);
        res.setDate(newDate);
        Reservation saved = reservationRepository.save(res);

        messageProducer.sendEvent("{\"type\": \"ReservationUpdated\", \"id\": \"" + saved.getId() + "\"}");

        return saved;
    }
}
