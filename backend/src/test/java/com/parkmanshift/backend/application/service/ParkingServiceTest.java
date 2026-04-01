package com.parkmanshift.backend.application.service;

import com.parkmanshift.backend.application.port.out.MessageProducerPort;
import com.parkmanshift.backend.application.port.out.ParkingSpotRepositoryPort;
import com.parkmanshift.backend.application.port.out.ReservationRepositoryPort;
import com.parkmanshift.backend.domain.exception.ReservationLimitExceededException;
import com.parkmanshift.backend.domain.exception.SpotNotAvailableException;
import com.parkmanshift.backend.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    @Mock
    private ParkingSpotRepositoryPort spotRepository;

    @Mock
    private ReservationRepositoryPort reservationRepository;

    @Mock
    private MessageProducerPort messageProducer;

    @InjectMocks
    private ParkingService parkingService;

    private final String EMP_ID = "E123";
    private final LocalDate DATE = LocalDate.of(2025, 5, 10);

    @Test
    public void reserveSpot_WithLessThan5Active_AndSpotFree_ShouldSuccess() {
        when(reservationRepository.findByEmployeeIdAndDateGreaterThanEqualAndStatusIn(eq(EMP_ID), any(LocalDate.class), anyList())).thenReturn(Collections.emptyList());
        when(reservationRepository.findByParkingSpotLabelAndDateAndStatusIn(eq("A01"), eq(DATE), anyList())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        Reservation res = parkingService.reserveSpot("A01", EMP_ID, UserRole.EMPLOYEE, DATE);

        assertNotNull(res);
        assertEquals("A01", res.getParkingSpotLabel());
        assertEquals(ReservationStatus.RESERVED, res.getStatus());
        verify(messageProducer).sendEvent(anyString());
    }

    @Test
    public void reserveSpot_ExceedingLimit_ShouldThrowException() {
        when(reservationRepository.findByEmployeeIdAndDateGreaterThanEqualAndStatusIn(eq(EMP_ID), any(LocalDate.class), anyList()))
            .thenReturn(List.of(new Reservation(), new Reservation(), new Reservation(), new Reservation(), new Reservation()));

        assertThrows(ReservationLimitExceededException.class, () -> parkingService.reserveSpot("A01", EMP_ID, UserRole.EMPLOYEE, DATE));
    }

    @Test
    public void reserveSpot_AlreadyReserved_ShouldThrowException() {
        when(reservationRepository.findByEmployeeIdAndDateGreaterThanEqualAndStatusIn(eq(EMP_ID), any(LocalDate.class), anyList())).thenReturn(Collections.emptyList());
        when(reservationRepository.findByParkingSpotLabelAndDateAndStatusIn(eq("A01"), eq(DATE), anyList()))
            .thenReturn(List.of(new Reservation()));

        assertThrows(SpotNotAvailableException.class, () -> parkingService.reserveSpot("A01", EMP_ID, UserRole.EMPLOYEE, DATE));
    }

    @Test
    public void reserveSpot_AsManager_With29Active_ShouldSuccess() {
        List<Reservation> manyReservations = new java.util.ArrayList<>();
        for (int i = 0; i < 29; i++) manyReservations.add(new Reservation());
        
        when(reservationRepository.findByEmployeeIdAndDateGreaterThanEqualAndStatusIn(eq(EMP_ID), any(LocalDate.class), anyList())).thenReturn(manyReservations);
        when(reservationRepository.findByParkingSpotLabelAndDateAndStatusIn(eq("A01"), eq(DATE), anyList())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        Reservation res = parkingService.reserveSpot("A01", EMP_ID, UserRole.MANAGER, DATE);

        assertNotNull(res);
    }

    @Test
    public void getParkingStateForDate_ShouldReturnSpotsWithStatus() {
        ParkingSpot spot1 = new ParkingSpot("A01", SpotType.ELECTRIC);
        ParkingSpot spot2 = new ParkingSpot("B01", SpotType.THERMAL);
        when(spotRepository.findAll()).thenReturn(List.of(spot1, spot2));

        Reservation res = new Reservation(UUID.randomUUID(), "A01", EMP_ID, DATE, ReservationStatus.RESERVED);
        when(reservationRepository.findByDate(DATE)).thenReturn(List.of(res));

        List<SpotState> state = parkingService.getParkingStateForDate(DATE);

        assertEquals(2, state.size());
        assertEquals("A01", state.get(0).getSpot().getLabel());
        assertEquals(SpotStatus.RESERVED, state.get(0).getStatus());

        assertEquals("B01", state.get(1).getSpot().getLabel());
        assertEquals(SpotStatus.FREE, state.get(1).getStatus());
    }

    @Test
    public void getDashboardStats_AsEmployee_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> parkingService.getDashboardStats(UserRole.EMPLOYEE, null, null));
    }

    @Test
    public void getDashboardStats_AsManager_ShouldReturnStats() {
        ParkingSpot spotElectric = new ParkingSpot("A01", SpotType.ELECTRIC);
        ParkingSpot spotThermal = new ParkingSpot("B01", SpotType.THERMAL);
        when(spotRepository.findAll()).thenReturn(List.of(spotElectric, spotThermal));

        Reservation resOcc = new Reservation(UUID.randomUUID(), "A01", "EMP1", DATE, ReservationStatus.OCCUPIED);
        Reservation resNoShow = new Reservation(UUID.randomUUID(), "B01", "EMP2", DATE, ReservationStatus.CANCELLED);

        when(reservationRepository.findAllReservations()).thenReturn(List.of(resOcc, resNoShow));

        DashboardStats stats = parkingService.getDashboardStats(UserRole.MANAGER, null, null);

        assertNotNull(stats);
        assertEquals(2, stats.getTotalReservations());
        assertEquals(50.0, stats.getOccupancyRate(), 0.01);
        assertEquals(50.0, stats.getNoShowProportion(), 0.01);
        assertEquals(50.0, stats.getElectricSpotProportion(), 0.01);
    }

    @Test
    public void getDashboardStats_WithFilters_ShouldReturnFilteredStats() {
        ParkingSpot spotElectric = new ParkingSpot("A01", SpotType.ELECTRIC);
        when(spotRepository.findAll()).thenReturn(List.of(spotElectric));

        Reservation resOcc1 = new Reservation(UUID.randomUUID(), "A01", "EMP1", LocalDate.of(2025, 5, 10), ReservationStatus.OCCUPIED);
        Reservation resOcc2 = new Reservation(UUID.randomUUID(), "A01", "EMP2", LocalDate.of(2025, 6, 12), ReservationStatus.OCCUPIED);

        when(reservationRepository.findAllReservations()).thenReturn(List.of(resOcc1, resOcc2));

        DashboardStats statsMonth = parkingService.getDashboardStats(UserRole.MANAGER, YearMonth.of(2025, 5), null);
        assertEquals(1, statsMonth.getTotalReservations());

        DashboardStats statsEmp = parkingService.getDashboardStats(UserRole.MANAGER, null, "EMP2");
        assertEquals(1, statsEmp.getTotalReservations());
        
        DashboardStats statsBoth = parkingService.getDashboardStats(UserRole.MANAGER, YearMonth.of(2025, 6), "EMP2");
        assertEquals(1, statsBoth.getTotalReservations());
        
        DashboardStats statsNone = parkingService.getDashboardStats(UserRole.MANAGER, YearMonth.of(2025, 5), "EMP2");
        assertEquals(0, statsNone.getTotalReservations());
    }

    @Test
    public void cancelUnconfirmedReservationsForToday_ShouldCancelReservedOnly() {
        LocalDate today = LocalDate.now();
        Reservation reserved1 = new Reservation(UUID.randomUUID(), "A01", "EMP1", today, ReservationStatus.RESERVED);
        Reservation reserved2 = new Reservation(UUID.randomUUID(), "B01", "EMP2", today, ReservationStatus.RESERVED);
        Reservation occupied = new Reservation(UUID.randomUUID(), "C01", "EMP3", today, ReservationStatus.OCCUPIED);
        Reservation cancelled = new Reservation(UUID.randomUUID(), "D01", "EMP4", today, ReservationStatus.CANCELLED);

        when(reservationRepository.findByDate(today)).thenReturn(List.of(reserved1, reserved2, occupied, cancelled));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        int result = parkingService.cancelUnconfirmedReservationsForToday();

        assertEquals(2, result);
        assertEquals(ReservationStatus.CANCELLED, reserved1.getStatus());
        assertEquals(ReservationStatus.CANCELLED, reserved2.getStatus());
        assertEquals(ReservationStatus.OCCUPIED, occupied.getStatus());
        verify(reservationRepository, times(2)).save(any(Reservation.class));
        verify(messageProducer, times(2)).sendEvent(contains("ReservationAutoCancelled"));
    }

    @Test
    public void cancelUnconfirmedReservationsForToday_WithNoReserved_ShouldReturnZero() {
        LocalDate today = LocalDate.now();
        Reservation occupied = new Reservation(UUID.randomUUID(), "A01", "EMP1", today, ReservationStatus.OCCUPIED);

        when(reservationRepository.findByDate(today)).thenReturn(List.of(occupied));

        int result = parkingService.cancelUnconfirmedReservationsForToday();

        assertEquals(0, result);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    public void updateReservation_AsOtherEmployee_ShouldThrowException() {
        UUID resId = UUID.randomUUID();
        Reservation res = new Reservation(resId, "A01", "EMP1", DATE, ReservationStatus.RESERVED);
        when(reservationRepository.findById(resId)).thenReturn(Optional.of(res));

        assertThrows(IllegalArgumentException.class, () ->
                parkingService.updateReservation(resId, "B01", DATE, "EMP2", UserRole.EMPLOYEE));
    }

    @Test
    public void updateReservation_AsOwner_WhenNewSpotFree_ShouldSuccess() {
        UUID resId = UUID.randomUUID();
        Reservation res = new Reservation(resId, "A01", "EMP1", DATE, ReservationStatus.RESERVED);
        when(reservationRepository.findById(resId)).thenReturn(Optional.of(res));
        // Mock checking availability for the new spot "B01"
        when(reservationRepository.findByParkingSpotLabelAndDateAndStatusIn(eq("B01"), eq(DATE), anyList()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        Reservation updated = parkingService.updateReservation(resId, "B01", DATE, "EMP1", UserRole.EMPLOYEE);

        assertEquals("B01", updated.getParkingSpotLabel());
        verify(messageProducer).sendEvent(contains("ReservationUpdated"));
    }

    @Test
    public void updateReservation_AsSecretary_WhenNewSpotFree_ShouldSuccess() {
        UUID resId = UUID.randomUUID();
        Reservation res = new Reservation(resId, "A01", "EMP1", DATE, ReservationStatus.RESERVED);
        when(reservationRepository.findById(resId)).thenReturn(Optional.of(res));
        when(reservationRepository.findByParkingSpotLabelAndDateAndStatusIn(eq("B01"), eq(DATE), anyList()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        Reservation updated = parkingService.updateReservation(resId, "B01", DATE, "SEC1", UserRole.SECRETARY);

        assertEquals("B01", updated.getParkingSpotLabel());
    }

    @Test
    public void updateReservation_AlreadyOccupiedByOther_ShouldThrowException() {
        UUID resId = UUID.randomUUID();
        Reservation res = new Reservation(resId, "A01", "EMP1", DATE, ReservationStatus.RESERVED);
        
        UUID otherResId = UUID.randomUUID();
        Reservation otherRes = new Reservation(otherResId, "B01", "EMP2", DATE, ReservationStatus.RESERVED);
        
        when(reservationRepository.findById(resId)).thenReturn(Optional.of(res));
        when(reservationRepository.findByParkingSpotLabelAndDateAndStatusIn(eq("B01"), eq(DATE), anyList()))
                .thenReturn(List.of(otherRes));

        assertThrows(SpotNotAvailableException.class, () ->
                parkingService.updateReservation(resId, "B01", DATE, "EMP1", UserRole.EMPLOYEE));
    }

    @Test
    public void updateReservation_StayingOnSameSpot_ShouldSuccess() {
        UUID resId = UUID.randomUUID();
        Reservation res = new Reservation(resId, "A01", "EMP1", DATE, ReservationStatus.RESERVED);
        
        when(reservationRepository.findById(resId)).thenReturn(Optional.of(res));
        // Availability check returns the same reservation
        when(reservationRepository.findByParkingSpotLabelAndDateAndStatusIn(eq("A01"), eq(DATE), anyList()))
                .thenReturn(List.of(res));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        Reservation updated = parkingService.updateReservation(resId, "A01", DATE, "EMP1", UserRole.EMPLOYEE);

        assertEquals("A01", updated.getParkingSpotLabel());
    }
}

