package com.parkmanshift.backend.application.port.in;

import com.parkmanshift.backend.domain.model.Reservation;
import com.parkmanshift.backend.domain.model.UserRole;
import java.util.List;

public interface GetReservationHistoryUseCase {
    List<Reservation> getHistory(String targetEmployeeId, String requesterId, UserRole requesterRole);
}
