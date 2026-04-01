package com.parkmanshift.backend.domain.model;

public class DashboardStats {
    private double occupancyRate;
    private double noShowProportion;
    private double electricSpotProportion;
    private int totalReservations;

    public DashboardStats(double occupancyRate, double noShowProportion, double electricSpotProportion, int totalReservations) {
        this.occupancyRate = occupancyRate;
        this.noShowProportion = noShowProportion;
        this.electricSpotProportion = electricSpotProportion;
        this.totalReservations = totalReservations;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public double getNoShowProportion() {
        return noShowProportion;
    }

    public double getElectricSpotProportion() {
        return electricSpotProportion;
    }

    public int getTotalReservations() {
        return totalReservations;
    }
}
