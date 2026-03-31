package com.parkmanshift.backend.domain.model;

public class ParkingSpot {
    private String label;
    private SpotType type;

    public ParkingSpot() {}

    public ParkingSpot(String label, SpotType type) {
        this.label = label;
        this.type = type;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public SpotType getType() { return type; }
    public void setType(SpotType type) { this.type = type; }
}
