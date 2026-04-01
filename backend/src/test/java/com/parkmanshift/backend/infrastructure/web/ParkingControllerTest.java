package com.parkmanshift.backend.infrastructure.web;

import com.parkmanshift.backend.application.port.in.GetReservationHistoryUseCase;
import com.parkmanshift.backend.application.port.in.ManageReservationUseCase;
import com.parkmanshift.backend.application.port.in.ReserveSpotUseCase;
import com.parkmanshift.backend.application.port.in.UpdateReservationUseCase;
import com.parkmanshift.backend.application.port.in.ViewParkingStateUseCase;
import com.parkmanshift.backend.application.port.in.GetDashboardStatsUseCase;
import com.parkmanshift.backend.domain.model.DashboardStats;
import com.parkmanshift.backend.domain.model.Reservation;
import com.parkmanshift.backend.domain.model.ReservationStatus;
import com.parkmanshift.backend.domain.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.PrincipalMethodArgumentResolver;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ParkingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ViewParkingStateUseCase viewParkingStateUseCase;

    @Mock
    private ReserveSpotUseCase reserveSpotUseCase;

    @Mock
    private ManageReservationUseCase manageReservationUseCase;

    @Mock
    private GetReservationHistoryUseCase getReservationHistoryUseCase;

    @Mock
    private GetDashboardStatsUseCase getDashboardStatsUseCase;

    @Mock
    private UpdateReservationUseCase updateReservationUseCase;

    @Mock
    private Authentication mockAuthentication;

    @InjectMocks
    private ParkingController parkingController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(parkingController)
                .setCustomArgumentResolvers(new PrincipalMethodArgumentResolver())
                .build();
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

        // Stub the mock authentication
        when(mockAuthentication.getName()).thenReturn("E123");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))).when(mockAuthentication).getAuthorities();

        String json = "{\"parkingSpotLabel\": \"A01\", \"date\": \"2025-05-10\"}";

        mockMvc.perform(post("/api/parking/reservations")
                .principal(mockAuthentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkingSpotLabel").value("A01"));
    }

    @Test
    public void testGetDashboardStats() throws Exception {
        DashboardStats stats = new DashboardStats(75.0, 10.0, 25.0, 40);
        when(mockAuthentication.getName()).thenReturn("admin");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))).when(mockAuthentication).getAuthorities();
        when(getDashboardStatsUseCase.getDashboardStats(eq(UserRole.MANAGER), any(), any())).thenReturn(stats);

        mockMvc.perform(get("/api/parking/dashboard")
                .principal(mockAuthentication)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupancyRate").value(75.0))
                 .andExpect(jsonPath("$.noShowProportion").value(10.0))
                .andExpect(jsonPath("$.electricSpotProportion").value(25.0))
                .andExpect(jsonPath("$.totalReservations").value(40));
    }

    @Test
    public void testUpdateReservation() throws Exception {
        UUID id = UUID.randomUUID();
        Reservation updated = new Reservation(id, "B01", "E123", LocalDate.of(2025, 5, 10), ReservationStatus.RESERVED);
        
        when(mockAuthentication.getName()).thenReturn("E123");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))).when(mockAuthentication).getAuthorities();
        when(updateReservationUseCase.updateReservation(eq(id), eq("B01"), eq(LocalDate.of(2025, 5, 10)), eq("E123"), eq(UserRole.EMPLOYEE)))
                .thenReturn(updated);

        String json = "{\"parkingSpotLabel\": \"B01\", \"date\": \"2025-05-10\"}";

        mockMvc.perform(put("/api/parking/reservations/" + id)
                .principal(mockAuthentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkingSpotLabel").value("B01"));
    }
}
