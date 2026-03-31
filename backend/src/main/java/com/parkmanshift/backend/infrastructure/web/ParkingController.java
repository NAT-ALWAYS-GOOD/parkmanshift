package com.parkmanshift.backend.infrastructure.web;

import com.parkmanshift.api.model.ReservationDto;
import com.parkmanshift.api.model.SpotStateDto;
import com.parkmanshift.backend.application.port.in.GetReservationHistoryUseCase;
import com.parkmanshift.backend.application.port.in.ManageReservationUseCase;
import com.parkmanshift.backend.application.port.in.ReserveSpotUseCase;
import com.parkmanshift.backend.application.port.in.ViewParkingStateUseCase;
import com.parkmanshift.backend.domain.model.Reservation;
import com.parkmanshift.backend.domain.model.UserRole;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parking")
public class ParkingController {

    private final ViewParkingStateUseCase viewParkingStateUseCase;
    private final ReserveSpotUseCase reserveSpotUseCase;
    private final ManageReservationUseCase manageReservationUseCase;
    private final GetReservationHistoryUseCase getReservationHistoryUseCase;

    public ParkingController(ViewParkingStateUseCase viewParkingStateUseCase, ReserveSpotUseCase reserveSpotUseCase, ManageReservationUseCase manageReservationUseCase, GetReservationHistoryUseCase getReservationHistoryUseCase) {
        this.viewParkingStateUseCase = viewParkingStateUseCase;
        this.reserveSpotUseCase = reserveSpotUseCase;
        this.manageReservationUseCase = manageReservationUseCase;
        this.getReservationHistoryUseCase = getReservationHistoryUseCase;
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
    public ReservationDto reserveSpot(@RequestBody com.parkmanshift.api.model.ReserveRequestDto request, java.security.Principal principal) {
        Authentication authentication = (Authentication) principal;
        String username = authentication.getName();
        UserRole role = getUserRole(authentication);
        
        Reservation reservation = reserveSpotUseCase.reserveSpot(
                request.getParkingSpotLabel(),
                username, // Use authenticated username as employeeId
                role,
                request.getDate()
        );
        return ApiMapper.toDto(reservation);
    }

    @GetMapping("/reservations/history")
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
    public ResponseEntity<Void> checkIn(@PathVariable UUID id, java.security.Principal principal) {
        Authentication authentication = (Authentication) principal;
        String username = authentication.getName();
        UserRole role = getUserRole(authentication);
        manageReservationUseCase.checkIn(id, username, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable UUID id, java.security.Principal principal) {
        Authentication authentication = (Authentication) principal;
        String username = authentication.getName();
        UserRole role = getUserRole(authentication);
        manageReservationUseCase.cancelReservation(id, username, role);
        return ResponseEntity.ok().build();
    }

    private UserRole getUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", ""))
                .map(UserRole::valueOf)
                .findFirst()
                .orElse(UserRole.EMPLOYEE);
    }
}
