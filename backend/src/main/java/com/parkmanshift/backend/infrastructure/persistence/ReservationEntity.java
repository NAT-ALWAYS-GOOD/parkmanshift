package com.parkmanshift.backend.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reservation")
public class ReservationEntity {

    @Id
    private UUID id;
    private String parkingSpotLabel;
    private String employeeId;
    private LocalDate reservationDate;
    private String status;

    public ReservationEntity() {}

    public ReservationEntity(UUID id, String parkingSpotLabel, String employeeId, LocalDate reservationDate, String status) {
        this.id = id;
        this.parkingSpotLabel = parkingSpotLabel;
        this.employeeId = employeeId;
        this.reservationDate = reservationDate;
        this.status = status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getParkingSpotLabel() { return parkingSpotLabel; }
    public void setParkingSpotLabel(String parkingSpotLabel) { this.parkingSpotLabel = parkingSpotLabel; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public LocalDate getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDate reservationDate) { this.reservationDate = reservationDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
