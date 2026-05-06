package com.serverless.platformselector.service;

import com.serverless.platformselector.config.AppDeploymentProperties;
import com.serverless.platformselector.config.AppObservabilityProperties;
import com.serverless.platformselector.entity.WorkerHeartbeat;
import com.serverless.platformselector.repository.WorkerHeartbeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class WorkerHeartbeatService {

    private final WorkerHeartbeatRepository workerHeartbeatRepository;
    private final AppDeploymentProperties deploymentProperties;
    private final AppObservabilityProperties observabilityProperties;

    public WorkerHeartbeatService(
            WorkerHeartbeatRepository workerHeartbeatRepository,
            AppDeploymentProperties deploymentProperties,
            AppObservabilityProperties observabilityProperties) {
        this.workerHeartbeatRepository = workerHeartbeatRepository;
        this.deploymentProperties = deploymentProperties;
        this.observabilityProperties = observabilityProperties;
    }

    public void recordIdleHeartbeat() {
        updateHeartbeat("IDLE", null, null);
    }

    public void recordJobStarted(UUID deploymentId) {
        updateHeartbeat("RUNNING", deploymentId, null);
    }

    public void recordJobCompleted(UUID deploymentId) {
        updateHeartbeat("IDLE", deploymentId, null);
    }

    public void recordJobFailed(UUID deploymentId, String errorMessage) {
        updateHeartbeat("FAILED", deploymentId, errorMessage);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getWorkerSummary() {
        Optional<WorkerHeartbeat> maybeHeartbeat = workerHeartbeatRepository.findTopByOrderByLastHeartbeatAtDesc();
        Map<String, Object> summary = new HashMap<>();

        if (maybeHeartbeat.isEmpty()) {
            summary.put("status", "UNKNOWN");
            summary.put("fresh", false);
            summary.put("stale", true);
            summary.put("serviceName", null);
            summary.put("workerId", null);
            summary.put("lastHeartbeatAt", null);
            summary.put("lastDeploymentId", null);
            summary.put("lastError", null);
            summary.put("staleAfterSeconds", observabilityProperties.getWorkerHeartbeatStaleAfterSeconds());
            return summary;
        }

        WorkerHeartbeat heartbeat = maybeHeartbeat.get();
        LocalDateTime now = LocalDateTime.now();
        long staleAfterSeconds = resolveStaleAfterSeconds(heartbeat.getLastStatus());
        boolean stale = heartbeat.getLastHeartbeatAt() == null
                || heartbeat.getLastHeartbeatAt().isBefore(now.minusSeconds(staleAfterSeconds));

        summary.put("status", heartbeat.getLastStatus());
        summary.put("fresh", !stale);
        summary.put("stale", stale);
        summary.put("serviceName", heartbeat.getServiceName());
        summary.put("workerId", heartbeat.getWorkerId());
        summary.put("lastHeartbeatAt", heartbeat.getLastHeartbeatAt());
        summary.put("lastDeploymentId", heartbeat.getLastDeploymentId());
        summary.put("lastError", heartbeat.getLastError());
        summary.put("staleAfterSeconds", staleAfterSeconds);
        return summary;
    }

    private long resolveStaleAfterSeconds(String status) {
        long configuredThreshold = observabilityProperties.getWorkerHeartbeatStaleAfterSeconds();
        if ("RUNNING".equalsIgnoreCase(status)) {
            return Math.max(configuredThreshold, deploymentProperties.getJobTimeoutSeconds() + 30);
        }
        return configuredThreshold;
    }

    private void updateHeartbeat(String status, UUID deploymentId, String errorMessage) {
        String workerId = deploymentProperties.getWorker().getWorkerId();
        WorkerHeartbeat heartbeat = workerHeartbeatRepository.findById(workerId).orElseGet(WorkerHeartbeat::new);

        heartbeat.setWorkerId(workerId);
        heartbeat.setServiceName(observabilityProperties.getServiceName());
        heartbeat.setLastHeartbeatAt(LocalDateTime.now());
        heartbeat.setLastStatus(status);
        if (deploymentId != null) {
            heartbeat.setLastDeploymentId(deploymentId);
        }
        heartbeat.setLastError(errorMessage);

        workerHeartbeatRepository.save(heartbeat);
    }
}
