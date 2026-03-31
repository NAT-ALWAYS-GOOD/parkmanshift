package com.parkmanshift.backend.application.port.in;

import com.parkmanshift.backend.domain.model.Reservation;
import com.parkmanshift.backend.domain.model.UserRole;
import java.time.LocalDate;

public interface ReserveSpotUseCase {
    Reservation reserveSpot(String parkingSpotLabel, String employeeId, UserRole userRole, LocalDate date);
}
