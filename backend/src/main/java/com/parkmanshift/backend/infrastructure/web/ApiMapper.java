package com.parkmanshift.backend.infrastructure.web;

import com.parkmanshift.api.model.*;
import com.parkmanshift.backend.domain.model.*;

/**
 * Mapper between domain models and OpenAPI-generated DTOs.
 * This is part of the infrastructure/web layer — it bridges the hexagonal
 * domain with the API contract defined in openapi.yaml.
 */
public final class ApiMapper {

    private ApiMapper() {
        // utility class
    }

    // --- Domain → DTO ---

    public static SpotStateDto toDto(SpotState domain) {
        SpotStateDto dto = new SpotStateDto();
        dto.setSpot(toDto(domain.getSpot()));
        dto.setStatus(toDto(domain.getStatus()));
        dto.setReservedBy(domain.getReservedBy());
        if (domain.getReservationStatus() != null) {
            dto.setReservationStatus(toDto(domain.getReservationStatus()));
        }
        if (domain.getReservationId() != null) {
            dto.setReservationId(domain.getReservationId());
        }
        return dto;
    }

    public static ParkingSpotDto toDto(ParkingSpot domain) {
        ParkingSpotDto dto = new ParkingSpotDto();
        dto.setLabel(domain.getLabel());
        dto.setType(toDto(domain.getType()));
        return dto;
    }

    public static ReservationDto toDto(Reservation domain) {
        ReservationDto dto = new ReservationDto();
        dto.setId(domain.getId());
        dto.setParkingSpotLabel(domain.getParkingSpotLabel());
        dto.setEmployeeId(domain.getEmployeeId());
        dto.setDate(domain.getDate());
        dto.setStatus(toDto(domain.getStatus()));
        return dto;
    }

    public static DashboardStatsDto toDto(DashboardStats domain) {
        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setOccupancyRate(domain.getOccupancyRate());
        dto.setNoShowProportion(domain.getNoShowProportion());
        dto.setElectricSpotProportion(domain.getElectricSpotProportion());
        dto.setTotalReservations(domain.getTotalReservations());
        return dto;
    }

    public static SpotStatusDto toDto(SpotStatus domain) {
        return SpotStatusDto.fromValue(domain.name());
    }

    public static UserDto toDto(User domain) {
        return toDto(domain, true);
    }

    public static UserDto toDto(User domain, boolean includeSensitiveInfo) {
        UserDto dto = new UserDto();
        dto.setId(domain.getId());
        dto.setUsername(domain.getUsername());
        dto.setFullName(domain.getFullName());
        if (domain.getRole() != null) {
            dto.setRole(UserDto.RoleEnum.fromValue(domain.getRole().name()));
        }
        if (includeSensitiveInfo) {
            dto.setCheckInCode(domain.getCheckInCode());
        }
        return dto;
    }

    public static CheckInVerificationDto toDto(CheckInVerification domain) {
        CheckInVerificationDto dto = new CheckInVerificationDto();
        dto.setUsername(domain.getUsername());
        dto.setFullName(domain.getFullName());
        dto.setExistingReservationSpotLabel(domain.getExistingReservationSpotLabel());
        dto.setConflict(domain.isConflict());
        return dto;
    }

    public static SpotTypeDto toDto(SpotType domain) {
        return SpotTypeDto.fromValue(domain.name());
    }

    public static ReservationStatusDto toDto(ReservationStatus domain) {
        return ReservationStatusDto.fromValue(domain.name());
    }

    // --- DTO → Domain ---

    public static SpotStatus toDomain(SpotStatusDto dto) {
        return SpotStatus.valueOf(dto.getValue());
    }

    public static SpotType toDomain(SpotTypeDto dto) {
        return SpotType.valueOf(dto.getValue());
    }

    public static ReservationStatus toDomain(ReservationStatusDto dto) {
        return ReservationStatus.valueOf(dto.getValue());
    }
}
