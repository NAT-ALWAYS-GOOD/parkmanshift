package com.parkmanshift.backend.application.port.out;

import com.parkmanshift.backend.domain.model.SkeletonLog;

public interface SkeletonLogRepositoryPort {
    SkeletonLog save(SkeletonLog log);
    long count();
}
