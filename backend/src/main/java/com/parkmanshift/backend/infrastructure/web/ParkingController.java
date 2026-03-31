package com.parkmanshift.backend.infrastructure.web;

import com.parkmanshift.api.model.ReservationDto;
import com.parkmanshift.api.model.SpotStateDto;
import com.parkmanshift.backend.application.port.in.ManageReservationUseCase;
import com.parkmanshift.backend.application.port.in.ReserveSpotUseCase;
import com.parkmanshift.backend.application.port.in.ViewParkingStateUseCase;
import com.parkmanshift.backend.domain.model.Reservation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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

    public ParkingController(ViewParkingStateUseCase viewParkingStateUseCase, ReserveSpotUseCase reserveSpotUseCase, ManageReservationUseCase manageReservationUseCase) {
        this.viewParkingStateUseCase = viewParkingStateUseCase;
        this.reserveSpotUseCase = reserveSpotUseCase;
        this.manageReservationUseCase = manageReservationUseCase;
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
    public ReservationDto reserveSpot(@RequestBody com.parkmanshift.api.model.ReserveRequestDto request) {
        Reservation reservation = reserveSpotUseCase.reserveSpot(
                request.getParkingSpotLabel(),
                request.getEmployeeId(),
                request.getDate()
        );
        return ApiMapper.toDto(reservation);
    }

    @PostMapping("/reservations/{id}/checkin")
    public ResponseEntity<Void> checkIn(@PathVariable UUID id, @RequestParam String employeeId) {
        manageReservationUseCase.checkIn(id, employeeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable UUID id, @RequestParam String employeeId) {
        manageReservationUseCase.cancelReservation(id, employeeId);
        return ResponseEntity.ok().build();
    }
}
