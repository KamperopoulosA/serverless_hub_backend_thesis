package com.serverless.platformselector.controller;

import com.serverless.platformselector.enums.DeploymentStatus;
import com.serverless.platformselector.repository.DeploymentRecordRepository;
import com.serverless.platformselector.service.WorkerHeartbeatService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ObservabilityControllerTest {

    @Test
    void shouldReturnGroupedObservabilitySections() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        DeploymentRecordRepository deploymentRecordRepository = mock(DeploymentRecordRepository.class);
        WorkerHeartbeatService workerHeartbeatService = mock(WorkerHeartbeatService.class);
        HealthEndpoint healthEndpoint = mock(HealthEndpoint.class);

        when(deploymentRecordRepository.countByDeploymentStatus(DeploymentStatus.QUEUED)).thenReturn(1L);
        when(deploymentRecordRepository.countByDeploymentStatus(DeploymentStatus.RUNNING)).thenReturn(2L);
        when(deploymentRecordRepository.countByDeploymentStatus(DeploymentStatus.SUCCESS)).thenReturn(3L);
        when(deploymentRecordRepository.countByDeploymentStatus(DeploymentStatus.FAILED)).thenReturn(4L);
        when(workerHeartbeatService.getWorkerSummary()).thenReturn(Map.of(
                "status", "IDLE",
                "fresh", true,
                "stale", false
        ));
        when(healthEndpoint.health()).thenReturn(Health.up().build());

        ObservabilityController controller = new ObservabilityController(
                meterRegistry,
                deploymentRecordRepository,
                workerHeartbeatService,
                healthEndpoint
        );

        Map<String, Object> summary = controller.summary();

        assertTrue(summary.containsKey("api"));
        assertTrue(summary.containsKey("deployments"));
        assertTrue(summary.containsKey("worker"));
        assertTrue(summary.containsKey("health"));
    }
}
