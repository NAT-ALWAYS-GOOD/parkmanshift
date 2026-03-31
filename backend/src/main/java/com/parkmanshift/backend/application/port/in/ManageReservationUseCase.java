package com.parkmanshift.backend.application.port.in;

import java.util.UUID;

public interface ManageReservationUseCase {
    void cancelReservation(UUID reservationId, String employeeId);
    void checkIn(UUID reservationId, String employeeId);
}
