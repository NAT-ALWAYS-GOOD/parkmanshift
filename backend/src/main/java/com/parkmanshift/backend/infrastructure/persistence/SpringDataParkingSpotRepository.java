package com.parkmanshift.backend.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataParkingSpotRepository extends JpaRepository<ParkingSpotEntity, String> {
}
