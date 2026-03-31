package com.parkmanshift.backend.application.port.in;

import com.parkmanshift.backend.domain.model.SpotState;
import java.time.LocalDate;
import java.util.List;

public interface ViewParkingStateUseCase {
    List<SpotState> getParkingStateForDate(LocalDate date);
}
