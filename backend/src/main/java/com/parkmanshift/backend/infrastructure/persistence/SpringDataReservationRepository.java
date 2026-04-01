package com.parkmanshift.backend.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataReservationRepository extends JpaRepository<ReservationEntity, UUID> {
    List<ReservationEntity> findByReservationDate(LocalDate reservationDate);
    List<ReservationEntity> findByEmployeeId(String employeeId);
    List<ReservationEntity> findByEmployeeIdAndStatusIn(String employeeId, List<String> statuses);
    List<ReservationEntity> findByEmployeeIdAndReservationDateAndStatusIn(String employeeId, LocalDate date, List<String> statuses);
    List<ReservationEntity> findByEmployeeIdAndReservationDateGreaterThanEqualAndStatusIn(String employeeId, LocalDate date, List<String> statuses);
    List<ReservationEntity> findByParkingSpotLabelAndReservationDateAndStatusIn(String parkingSpotLabel, LocalDate reservationDate, List<String> statuses);
}
