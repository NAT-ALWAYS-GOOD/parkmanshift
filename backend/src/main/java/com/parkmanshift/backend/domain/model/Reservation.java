package com.parkmanshift.backend.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public class Reservation {
    private UUID id;
    private String parkingSpotLabel;
    private String employeeId;
    private LocalDate date;
    private ReservationStatus status;

    public Reservation() {}

    public Reservation(UUID id, String parkingSpotLabel, String employeeId, LocalDate date, ReservationStatus status) {
        this.id = id;
        this.parkingSpotLabel = parkingSpotLabel;
        this.employeeId = employeeId;
        this.date = date;
        this.status = status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getParkingSpotLabel() { return parkingSpotLabel; }
    public void setParkingSpotLabel(String parkingSpotLabel) { this.parkingSpotLabel = parkingSpotLabel; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
}
