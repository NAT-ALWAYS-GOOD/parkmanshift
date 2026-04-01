package com.parkmanshift.backend.application.port.in;

import com.parkmanshift.backend.domain.model.Reservation;
import com.parkmanshift.backend.domain.model.UserRole;
import java.time.LocalDate;
import java.util.UUID;

public interface UpdateReservationUseCase {
    Reservation updateReservation(UUID reservationId, String newSpotLabel, LocalDate newDate, String requesterId, UserRole requesterRole);
}
