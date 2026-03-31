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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        when(reservationRepository.findByEmployeeIdAndStatusIn(eq(EMP_ID), anyList())).thenReturn(Collections.emptyList());
        when(reservationRepository.findByParkingSpotLabelAndDateAndStatusIn(eq("A01"), eq(DATE), anyList())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        Reservation res = parkingService.reserveSpot("A01", EMP_ID, DATE);

        assertNotNull(res);
        assertEquals("A01", res.getParkingSpotLabel());
        assertEquals(ReservationStatus.RESERVED, res.getStatus());
        verify(messageProducer).sendEvent(anyString());
    }

    @Test
    public void reserveSpot_ExceedingLimit_ShouldThrowException() {
        when(reservationRepository.findByEmployeeIdAndStatusIn(eq(EMP_ID), anyList()))
            .thenReturn(List.of(new Reservation(), new Reservation(), new Reservation(), new Reservation(), new Reservation()));

        assertThrows(ReservationLimitExceededException.class, () -> parkingService.reserveSpot("A01", EMP_ID, DATE));
    }

    @Test
    public void reserveSpot_AlreadyReserved_ShouldThrowException() {
        when(reservationRepository.findByEmployeeIdAndStatusIn(eq(EMP_ID), anyList())).thenReturn(Collections.emptyList());
        when(reservationRepository.findByParkingSpotLabelAndDateAndStatusIn(eq("A01"), eq(DATE), anyList()))
            .thenReturn(List.of(new Reservation()));

        assertThrows(SpotNotAvailableException.class, () -> parkingService.reserveSpot("A01", EMP_ID, DATE));
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
}
