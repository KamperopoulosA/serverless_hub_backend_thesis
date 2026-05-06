package com.serverless.platformselector.service;

import com.serverless.platformselector.config.AppDeploymentProperties;
import com.serverless.platformselector.config.AppObservabilityProperties;
import com.serverless.platformselector.entity.WorkerHeartbeat;
import com.serverless.platformselector.repository.WorkerHeartbeatRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkerHeartbeatServiceTest {

    @Test
    void shouldPersistHeartbeatForStartedJob() {
        WorkerHeartbeatRepository repository = mock(WorkerHeartbeatRepository.class);
        AppDeploymentProperties deploymentProperties = new AppDeploymentProperties();
        deploymentProperties.getWorker().setWorkerId("worker-1");
        AppObservabilityProperties observabilityProperties = new AppObservabilityProperties();
        observabilityProperties.setServiceName("worker-service");

        when(repository.findById("worker-1")).thenReturn(Optional.empty());

        WorkerHeartbeatService service = new WorkerHeartbeatService(repository, deploymentProperties, observabilityProperties);
        UUID deploymentId = UUID.randomUUID();

        service.recordJobStarted(deploymentId);

        verify(repository).save(any(WorkerHeartbeat.class));
    }

    @Test
    void shouldReportStaleWorkerSummaryWhenHeartbeatIsOld() {
        WorkerHeartbeatRepository repository = mock(WorkerHeartbeatRepository.class);
        AppDeploymentProperties deploymentProperties = new AppDeploymentProperties();
        AppObservabilityProperties observabilityProperties = new AppObservabilityProperties();
        observabilityProperties.setWorkerHeartbeatStaleAfterSeconds(10);

        WorkerHeartbeat heartbeat = new WorkerHeartbeat();
        heartbeat.setWorkerId("worker-1");
        heartbeat.setServiceName("worker-service");
        heartbeat.setLastStatus("IDLE");
        heartbeat.setLastHeartbeatAt(LocalDateTime.now().minusSeconds(60));

        when(repository.findTopByOrderByLastHeartbeatAtDesc()).thenReturn(Optional.of(heartbeat));

        WorkerHeartbeatService service = new WorkerHeartbeatService(repository, deploymentProperties, observabilityProperties);
        Map<String, Object> summary = service.getWorkerSummary();

        assertEquals("IDLE", summary.get("status"));
        assertTrue((Boolean) summary.get("stale"));
        assertFalse((Boolean) summary.get("fresh"));
    }

    @Test
    void shouldExtendFreshnessWindowForRunningJobs() {
        WorkerHeartbeatRepository repository = mock(WorkerHeartbeatRepository.class);
        AppDeploymentProperties deploymentProperties = new AppDeploymentProperties();
        deploymentProperties.setJobTimeoutSeconds(600);
        AppObservabilityProperties observabilityProperties = new AppObservabilityProperties();
        observabilityProperties.setWorkerHeartbeatStaleAfterSeconds(10);

        WorkerHeartbeat heartbeat = new WorkerHeartbeat();
        heartbeat.setWorkerId("worker-1");
        heartbeat.setServiceName("worker-service");
        heartbeat.setLastStatus("RUNNING");
        heartbeat.setLastHeartbeatAt(LocalDateTime.now().minusSeconds(45));

        when(repository.findTopByOrderByLastHeartbeatAtDesc()).thenReturn(Optional.of(heartbeat));

        WorkerHeartbeatService service = new WorkerHeartbeatService(repository, deploymentProperties, observabilityProperties);
        Map<String, Object> summary = service.getWorkerSummary();

        assertEquals("RUNNING", summary.get("status"));
        assertFalse((Boolean) summary.get("stale"));
        assertTrue((Boolean) summary.get("fresh"));
        assertEquals(630L, summary.get("staleAfterSeconds"));
    }
}
