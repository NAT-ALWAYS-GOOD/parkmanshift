package com.parkmanshift.backend.application.port.in;

import java.time.LocalDate;
import com.parkmanshift.backend.domain.model.Reservation;

public interface ReserveSpotUseCase {
    Reservation reserveSpot(String parkingSpotLabel, String employeeId, LocalDate date);
}
