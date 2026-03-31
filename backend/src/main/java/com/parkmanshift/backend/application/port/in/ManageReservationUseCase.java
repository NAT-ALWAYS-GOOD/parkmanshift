package com.parkmanshift.backend.application.port.in;

import com.parkmanshift.backend.domain.model.UserRole;
import java.util.UUID;

public interface ManageReservationUseCase {
    void cancelReservation(UUID reservationId, String employeeId, UserRole userRole);
    void checkIn(UUID reservationId, String employeeId, UserRole userRole);
}
