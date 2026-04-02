package com.parkmanshift.backend.application.port.in;

import com.parkmanshift.backend.domain.model.CheckInVerification;

public interface CheckInWithCodeUseCase {
    CheckInVerification verifyCheckIn(String spotLabel, String code);
    void confirmCheckIn(String spotLabel, String code);
}
