package com.parkmanshift.backend.application.port.in;

import com.parkmanshift.backend.domain.model.DashboardStats;
import com.parkmanshift.backend.domain.model.UserRole;

import java.time.YearMonth;

public interface GetDashboardStatsUseCase {
    DashboardStats getDashboardStats(UserRole requesterRole, YearMonth yearMonth, String employeeId);
}
