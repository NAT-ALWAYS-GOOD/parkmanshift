package com.parkmanshift.backend.infrastructure.persistence;

import com.parkmanshift.backend.application.port.out.SkeletonLogRepositoryPort;
import com.parkmanshift.backend.domain.model.SkeletonLog;
import org.springframework.stereotype.Component;

@Component
public class SkeletonLogPersistenceAdapter implements SkeletonLogRepositoryPort {

    private final SpringDataSkeletonLogRepository repository;

    public SkeletonLogPersistenceAdapter(SpringDataSkeletonLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public SkeletonLog save(SkeletonLog log) {
        SkeletonLogEntity entity = new SkeletonLogEntity(log.getMessage(), log.getTimestamp());
        entity = repository.save(entity);
        log.setId(entity.getId());
        return log;
    }

    @Override
    public long count() {
        return repository.count();
    }
}
