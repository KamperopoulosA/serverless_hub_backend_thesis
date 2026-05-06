package com.serverless.platformselector.repository;

import com.serverless.platformselector.entity.DeploymentRecord;
import com.serverless.platformselector.enums.CloudProvider;
import com.serverless.platformselector.enums.DeploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeploymentRecordRepository extends JpaRepository<DeploymentRecord, UUID>, DeploymentRecordRepositoryCustom {

    List<DeploymentRecord> findByOwnerUserIdOrderByCreatedAtDesc(Integer ownerUserId);

    Optional<DeploymentRecord> findByIdAndOwnerUserId(UUID id, Integer ownerUserId);

    long countByDeploymentStatus(DeploymentStatus deploymentStatus);

    long countByDeploymentStatusAndProvider(DeploymentStatus deploymentStatus, CloudProvider provider);
}
