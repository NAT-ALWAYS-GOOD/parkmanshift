package com.parkmanshift.backend.infrastructure.web;

import com.parkmanshift.backend.application.port.in.ManageReservationUseCase;
import com.parkmanshift.backend.application.port.in.ReserveSpotUseCase;
import com.parkmanshift.backend.application.port.in.ViewParkingStateUseCase;
import com.parkmanshift.backend.domain.model.Reservation;
import com.parkmanshift.backend.domain.model.ReservationStatus;
import com.parkmanshift.backend.domain.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ParkingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ViewParkingStateUseCase viewParkingStateUseCase;

    @Mock
    private ReserveSpotUseCase reserveSpotUseCase;

    @Mock
    private ManageReservationUseCase manageReservationUseCase;

    @InjectMocks
    private ParkingController parkingController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(parkingController).build();
    }

    @Test
    public void testGetState() throws Exception {
        when(viewParkingStateUseCase.getParkingStateForDate(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/parking/state").param("date", "2025-05-10"))
                .andExpect(status().isOk());
    }

    @Test
    public void testReserveSpot() throws Exception {
        Reservation res = new Reservation(UUID.randomUUID(), "A01", "E123", LocalDate.of(2025, 5, 10), ReservationStatus.RESERVED);
        when(reserveSpotUseCase.reserveSpot(eq("A01"), eq("E123"), eq(UserRole.EMPLOYEE), eq(LocalDate.of(2025, 5, 10)))).thenReturn(res);

        String json = "{\"parkingSpotLabel\": \"A01\", \"date\": \"2025-05-10\"}";

        mockMvc.perform(post("/api/parking/reservations")
                .with(user("E123").roles("EMPLOYEE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkingSpotLabel").value("A01"));
    }
}
