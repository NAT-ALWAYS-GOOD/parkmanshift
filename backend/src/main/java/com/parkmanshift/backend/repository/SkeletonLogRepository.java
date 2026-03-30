package com.parkmanshift.backend.repository;

import com.parkmanshift.backend.entity.SkeletonLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkeletonLogRepository extends JpaRepository<SkeletonLog, Long> {
}
