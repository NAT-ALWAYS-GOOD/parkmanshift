package com.parkmanshift.backend.domain.model;

public class CheckInVerification {
    private String username;
    private String fullName;
    private String existingReservationSpotLabel;
    private boolean conflict;

    public CheckInVerification(String username, String fullName, String existingReservationSpotLabel, boolean conflict) {
        this.username = username;
        this.fullName = fullName;
        this.existingReservationSpotLabel = existingReservationSpotLabel;
        this.conflict = conflict;
    }

    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getExistingReservationSpotLabel() { return existingReservationSpotLabel; }
    public boolean isConflict() { return conflict; }
}
