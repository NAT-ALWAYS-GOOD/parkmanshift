package com.parkmanshift.backend.application.port.out;

import com.parkmanshift.backend.domain.model.Reservation;
import com.parkmanshift.backend.domain.model.ReservationStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepositoryPort {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(UUID id);
    List<Reservation> findByDate(LocalDate date);
    List<Reservation> findByEmployeeIdAndStatusIn(String employeeId, List<ReservationStatus> statuses);
    List<Reservation> findByParkingSpotLabelAndDateAndStatusIn(String label, LocalDate date, List<ReservationStatus> statuses);
}
