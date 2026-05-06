package com.serverless.platformselector.repository;

import com.serverless.platformselector.entity.DeploymentRecord;
import com.serverless.platformselector.enums.DeploymentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DeploymentRecordRepositoryImpl implements DeploymentRecordRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Optional<DeploymentRecord> claimNextQueuedJob(String workerId) {
        List<?> rows = entityManager.createNativeQuery("""
                SELECT id
                FROM deployment_records
                WHERE deployment_status = 'QUEUED'
                ORDER BY created_at ASC
                LIMIT 1
                FOR UPDATE SKIP LOCKED
                """)
            .getResultList();

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Object rawId = rows.get(0);
        UUID id = rawId instanceof UUID ? (UUID) rawId : UUID.fromString(rawId.toString());
        DeploymentRecord record = entityManager.find(DeploymentRecord.class, id);

        if (record == null) {
            return Optional.empty();
        }

        record.setDeploymentStatus(DeploymentStatus.RUNNING);
        record.setWorkerId(workerId);
        record.setStartedAt(LocalDateTime.now());
        entityManager.flush();
        return Optional.of(record);
    }
}
