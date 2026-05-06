package com.serverless.platformselector.repository;

import com.serverless.platformselector.entity.DeploymentRecord;

import java.util.Optional;

public interface DeploymentRecordRepositoryCustom {

    Optional<DeploymentRecord> claimNextQueuedJob(String workerId);
}
