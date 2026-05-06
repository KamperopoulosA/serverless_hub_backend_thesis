package com.serverless.platformselector.repository;

import com.serverless.platformselector.entity.WorkerHeartbeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkerHeartbeatRepository extends JpaRepository<WorkerHeartbeat, String> {

    Optional<WorkerHeartbeat> findTopByOrderByLastHeartbeatAtDesc();
}
