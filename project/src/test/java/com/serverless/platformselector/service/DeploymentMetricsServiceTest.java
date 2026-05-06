package com.serverless.platformselector.service;

import com.serverless.platformselector.entity.DeploymentRecord;
import com.serverless.platformselector.enums.CloudProvider;
import com.serverless.platformselector.enums.DeploymentStatus;
import com.serverless.platformselector.repository.DeploymentRecordRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeploymentMetricsServiceTest {

    @Test
    void shouldRecordDeploymentLifecycleMetrics() {
        DeploymentRecordRepository repository = mock(DeploymentRecordRepository.class);
        when(repository.countByDeploymentStatus(DeploymentStatus.QUEUED)).thenReturn(0L);
        when(repository.countByDeploymentStatus(DeploymentStatus.RUNNING)).thenReturn(0L);
        when(repository.countByDeploymentStatusAndProvider(DeploymentStatus.QUEUED, CloudProvider.AWS)).thenReturn(0L);
        when(repository.countByDeploymentStatusAndProvider(DeploymentStatus.QUEUED, CloudProvider.GCP)).thenReturn(0L);
        when(repository.countByDeploymentStatusAndProvider(DeploymentStatus.RUNNING, CloudProvider.AWS)).thenReturn(0L);
        when(repository.countByDeploymentStatusAndProvider(DeploymentStatus.RUNNING, CloudProvider.GCP)).thenReturn(0L);

        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        DeploymentMetricsService service = new DeploymentMetricsService(meterRegistry, repository);

        DeploymentRecord record = new DeploymentRecord();
        record.setProvider(CloudProvider.AWS);
        record.setStartedAt(LocalDateTime.now().minusSeconds(4));
        record.setFinishedAt(LocalDateTime.now());

        service.recordCreated(CloudProvider.AWS);
        service.recordStarted(record);
        service.recordCompleted(record);

        assertEquals(1.0, meterRegistry.get("deployment.jobs.created").tag("provider", "AWS").tag("status", "QUEUED").counter().count());
        assertEquals(1.0, meterRegistry.get("deployment.jobs.started").tag("provider", "AWS").tag("status", "RUNNING").counter().count());
        assertEquals(1.0, meterRegistry.get("deployment.jobs.completed").tag("provider", "AWS").tag("status", "SUCCESS").counter().count());
        assertEquals(1L, meterRegistry.get("deployment.jobs.execution").tag("provider", "AWS").tag("status", "SUCCESS").timer().count());
    }
}
