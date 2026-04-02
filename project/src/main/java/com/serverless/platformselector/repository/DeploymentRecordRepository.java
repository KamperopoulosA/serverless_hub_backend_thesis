package com.serverless.platformselector.repository;

import com.serverless.platformselector.entity.DeploymentRecord;
import com.serverless.platformselector.enums.DeploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeploymentRecordRepository extends JpaRepository<DeploymentRecord, UUID> {
    
    List<DeploymentRecord> findByUserId(UUID userId);

    //List<DeploymentRecord> findByUserId(String userId);

    Page<DeploymentRecord> findByUserId(UUID userId, Pageable pageable);
    
    List<DeploymentRecord> findByUserIdAndDeploymentStatus(UUID userId, DeploymentStatus status);
    
    List<DeploymentRecord> findByPlatformId(UUID platformId);
    
    Page<DeploymentRecord> findByUserIdAndPlatformId(UUID userId, UUID platformId, Pageable pageable);
}