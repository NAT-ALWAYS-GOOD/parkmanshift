package com.parkmanshift.backend.domain.model;

public class SpotState {
    private ParkingSpot spot;
    private SpotStatus status;
    private String reservedBy;
    private ReservationStatus reservationStatus;
    private java.util.UUID reservationId;

    public SpotState(ParkingSpot spot, SpotStatus status) {
        this.spot = spot;
        this.status = status;
    }

    public SpotState(ParkingSpot spot, SpotStatus status, String reservedBy, ReservationStatus reservationStatus, java.util.UUID reservationId) {
        this.spot = spot;
        this.status = status;
        this.reservedBy = reservedBy;
        this.reservationStatus = reservationStatus;
        this.reservationId = reservationId;
    }

    public ParkingSpot getSpot() { return spot; }
    public SpotStatus getStatus() { return status; }
    public String getReservedBy() { return reservedBy; }
    public ReservationStatus getReservationStatus() { return reservationStatus; }
    public java.util.UUID getReservationId() { return reservationId; }
}
