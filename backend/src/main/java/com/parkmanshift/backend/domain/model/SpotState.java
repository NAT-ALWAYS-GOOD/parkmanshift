package com.parkmanshift.backend.domain.model;

public class SpotState {
    private ParkingSpot spot;
    private SpotStatus status;

    public SpotState(ParkingSpot spot, SpotStatus status) {
        this.spot = spot;
        this.status = status;
    }

    public ParkingSpot getSpot() { return spot; }
    public SpotStatus getStatus() { return status; }
}
