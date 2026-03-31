package com.parkmanshift.backend.application.port.out;

import com.parkmanshift.backend.domain.model.ParkingSpot;
import java.util.List;
import java.util.Optional;

public interface ParkingSpotRepositoryPort {
    List<ParkingSpot> findAll();
    Optional<ParkingSpot> findByLabel(String label);
    void saveAll(List<ParkingSpot> spots);
}
