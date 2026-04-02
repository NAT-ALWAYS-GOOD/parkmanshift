package com.parkmanshift.backend.infrastructure.web;

import com.parkmanshift.api.model.ReservationDto;
import com.parkmanshift.api.model.SpotStateDto;
import com.parkmanshift.backend.application.port.in.GetReservationHistoryUseCase;
import com.parkmanshift.backend.application.port.in.ManageReservationUseCase;
import com.parkmanshift.backend.application.port.in.ReserveSpotUseCase;
import com.parkmanshift.backend.application.port.in.UpdateReservationUseCase;
import com.parkmanshift.backend.application.port.in.ViewParkingStateUseCase;
import com.parkmanshift.backend.application.port.in.GetDashboardStatsUseCase;
import com.parkmanshift.backend.application.port.in.CheckInWithCodeUseCase;
import com.parkmanshift.api.model.DashboardStatsDto;
import com.parkmanshift.backend.domain.model.Reservation;
import com.parkmanshift.backend.domain.model.UserRole;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parking")
public class ParkingController {

    private final ViewParkingStateUseCase viewParkingStateUseCase;
    private final ReserveSpotUseCase reserveSpotUseCase;
    private final ManageReservationUseCase manageReservationUseCase;
    private final GetReservationHistoryUseCase getReservationHistoryUseCase;
    private final GetDashboardStatsUseCase getDashboardStatsUseCase;
    private final UpdateReservationUseCase updateReservationUseCase;
    private final CheckInWithCodeUseCase checkInWithCodeUseCase;

    public ParkingController(
            @org.springframework.beans.factory.annotation.Qualifier("viewParkingStateUseCase") ViewParkingStateUseCase viewParkingStateUseCase,
            @org.springframework.beans.factory.annotation.Qualifier("reserveSpotUseCase") ReserveSpotUseCase reserveSpotUseCase,
            @org.springframework.beans.factory.annotation.Qualifier("manageReservationUseCase") ManageReservationUseCase manageReservationUseCase,
            @org.springframework.beans.factory.annotation.Qualifier("getReservationHistoryUseCase") GetReservationHistoryUseCase getReservationHistoryUseCase,
            @org.springframework.beans.factory.annotation.Qualifier("getDashboardStatsUseCase") GetDashboardStatsUseCase getDashboardStatsUseCase,
            @org.springframework.beans.factory.annotation.Qualifier("updateReservationUseCase") UpdateReservationUseCase updateReservationUseCase,
            @org.springframework.beans.factory.annotation.Qualifier("checkInWithCodeUseCase") CheckInWithCodeUseCase checkInWithCodeUseCase) {
        this.viewParkingStateUseCase = viewParkingStateUseCase;
        this.reserveSpotUseCase = reserveSpotUseCase;
        this.manageReservationUseCase = manageReservationUseCase;
        this.getReservationHistoryUseCase = getReservationHistoryUseCase;
        this.getDashboardStatsUseCase = getDashboardStatsUseCase;
        this.updateReservationUseCase = updateReservationUseCase;
        this.checkInWithCodeUseCase = checkInWithCodeUseCase;
    }

    @GetMapping("/public/check-in/verify")
    public com.parkmanshift.api.model.CheckInVerificationDto verifyCheckIn(
            @RequestParam String spotLabel,
            @RequestParam String code) {
        return ApiMapper.toDto(checkInWithCodeUseCase.verifyCheckIn(spotLabel, code));
    }

    @GetMapping("/public/spots/{label}/status")
    public ResponseEntity<com.parkmanshift.api.model.SpotStatusDto> getSpotStatus(@PathVariable String label) {
        return viewParkingStateUseCase.getParkingStateForDate(LocalDate.now())
                .stream()
                .filter(s -> s.getSpot().getLabel().equals(label))
                .findFirst()
                .map(s -> ResponseEntity.ok(ApiMapper.toDto(s.getStatus())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/public/check-in/confirm")
    public ResponseEntity<Void> confirmCheckIn(
            @RequestParam String spotLabel,
            @RequestParam String code) {
        checkInWithCodeUseCase.confirmCheckIn(spotLabel, code);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('MANAGER', 'SECRETARY')")
    public DashboardStatsDto getDashboardStats(
            @RequestParam(required = false) YearMonth yearMonth,
            @RequestParam(required = false) String employeeId,
            java.security.Principal principal) {
        Authentication authentication = (Authentication) principal;
        UserRole role = getUserRole(authentication);
        return ApiMapper.toDto(getDashboardStatsUseCase.getDashboardStats(role, yearMonth, employeeId));
    }

    @GetMapping("/state")
    public List<SpotStateDto> getParkingState(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return viewParkingStateUseCase.getParkingStateForDate(date)
                .stream()
                .map(ApiMapper::toDto)
                .toList();
    }

    @PostMapping("/reservations")
    @PreAuthorize("isAuthenticated()")
    public ReservationDto reserveSpot(@RequestBody com.parkmanshift.api.model.ReserveRequestDto request, java.security.Principal principal) {
        Authentication authentication = (Authentication) principal;
        String username = authentication.getName();
        UserRole role = getUserRole(authentication);
        
        String targetEmployee = (role == UserRole.SECRETARY && request.getTargetUsername() != null)
                ? request.getTargetUsername()
                : username;

        Reservation reservation = reserveSpotUseCase.reserveSpot(
                request.getParkingSpotLabel(),
                targetEmployee,
                role,
                request.getDate()
        );
        return ApiMapper.toDto(reservation);
    }

    @GetMapping("/reservations/history")
    @PreAuthorize("isAuthenticated()")
    public List<ReservationDto> getHistory(
            @RequestParam(required = false) String employeeId,
            java.security.Principal principal) {
        Authentication authentication = (Authentication) principal;
        String username = authentication.getName();
        UserRole role = getUserRole(authentication);

        String targetId = (employeeId != null) ? employeeId : username;

        return getReservationHistoryUseCase.getHistory(targetId, username, role)
                .stream()
                .map(ApiMapper::toDto)
                .toList();
    }

    @PostMapping("/reservations/{id}/checkin")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> checkIn(@PathVariable UUID id, java.security.Principal principal) {
        Authentication authentication = (Authentication) principal;
        String username = authentication.getName();
        UserRole role = getUserRole(authentication);
        manageReservationUseCase.checkIn(id, username, role);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reservations/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelReservation(@PathVariable UUID id, java.security.Principal principal) {
        Authentication authentication = (Authentication) principal;
        String username = authentication.getName();
        UserRole role = getUserRole(authentication);
        manageReservationUseCase.cancelReservation(id, username, role);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reservations/{id}")
    @PreAuthorize("isAuthenticated()")
    public ReservationDto updateReservation(
            @PathVariable UUID id,
            @RequestBody com.parkmanshift.api.model.ReserveRequestDto request,
            java.security.Principal principal) {
        Authentication authentication = (Authentication) principal;
        String username = authentication.getName();
        UserRole role = getUserRole(authentication);
        
        Reservation updated = updateReservationUseCase.updateReservation(
                id,
                request.getParkingSpotLabel(),
                request.getDate(),
                username,
                role
        );
        return ApiMapper.toDto(updated);
    }

    private UserRole getUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
                .filter(auth -> {
                    try {
                        UserRole.valueOf(auth);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(UserRole::valueOf)
                .findFirst()
                .orElse(UserRole.EMPLOYEE);
    }
}
