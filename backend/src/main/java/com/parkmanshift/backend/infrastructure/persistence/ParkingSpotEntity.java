package com.parkmanshift.backend.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parking_spot")
public class ParkingSpotEntity {

    @Id
    private String label; // A01 - F10
    
    // ELECTRIC or THERMAL
    private String type;

    public ParkingSpotEntity() {}

    public ParkingSpotEntity(String label, String type) {
        this.label = label;
        this.type = type;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
