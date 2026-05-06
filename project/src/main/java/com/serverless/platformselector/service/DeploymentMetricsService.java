package com.serverless.platformselector.service;

import com.serverless.platformselector.entity.DeploymentRecord;
import com.serverless.platformselector.enums.CloudProvider;
import com.serverless.platformselector.enums.DeploymentStatus;
import com.serverless.platformselector.repository.DeploymentRecordRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumSet;

@Service
public class DeploymentMetricsService {

    private final MeterRegistry meterRegistry;
    private final DeploymentRecordRepository deploymentRecordRepository;

    public DeploymentMetricsService(
            MeterRegistry meterRegistry,
            DeploymentRecordRepository deploymentRecordRepository) {
        this.meterRegistry = meterRegistry;
        this.deploymentRecordRepository = deploymentRecordRepository;
        registerGauges();
    }

    public void recordCreated(CloudProvider provider) {
        meterRegistry.counter(
                "deployment.jobs.created",
                "provider", tagValue(provider),
                "status", DeploymentStatus.QUEUED.name()
        ).increment();
    }

    public void recordStarted(DeploymentRecord record) {
        meterRegistry.counter(
                "deployment.jobs.started",
                "provider", tagValue(record.getProvider()),
                "status", DeploymentStatus.RUNNING.name()
        ).increment();
    }

    public void recordCompleted(DeploymentRecord record) {
        meterRegistry.counter(
                "deployment.jobs.completed",
                "provider", tagValue(record.getProvider()),
                "status", DeploymentStatus.SUCCESS.name()
        ).increment();
        recordExecutionDuration(record, DeploymentStatus.SUCCESS);
    }

    public void recordFailed(DeploymentRecord record) {
        meterRegistry.counter(
                "deployment.jobs.failed",
                "provider", tagValue(record.getProvider()),
                "status", DeploymentStatus.FAILED.name()
        ).increment();
        recordExecutionDuration(record, DeploymentStatus.FAILED);
    }

    private void registerGauges() {
        Gauge.builder("deployment.jobs.queued", () -> deploymentRecordRepository.countByDeploymentStatus(DeploymentStatus.QUEUED))
                .tag("provider", "ALL")
                .tag("status", DeploymentStatus.QUEUED.name())
                .register(meterRegistry);

        Gauge.builder("deployment.jobs.running", () -> deploymentRecordRepository.countByDeploymentStatus(DeploymentStatus.RUNNING))
                .tag("provider", "ALL")
                .tag("status", DeploymentStatus.RUNNING.name())
                .register(meterRegistry);

        for (CloudProvider provider : EnumSet.allOf(CloudProvider.class)) {
            Gauge.builder("deployment.jobs.queued",
                            () -> deploymentRecordRepository.countByDeploymentStatusAndProvider(DeploymentStatus.QUEUED, provider))
                    .tag("provider", provider.name())
                    .tag("status", DeploymentStatus.QUEUED.name())
                    .register(meterRegistry);

            Gauge.builder("deployment.jobs.running",
                            () -> deploymentRecordRepository.countByDeploymentStatusAndProvider(DeploymentStatus.RUNNING, provider))
                    .tag("provider", provider.name())
                    .tag("status", DeploymentStatus.RUNNING.name())
                    .register(meterRegistry);
        }
    }

    private void recordExecutionDuration(DeploymentRecord record, DeploymentStatus finalStatus) {
        LocalDateTime startedAt = record.getStartedAt();
        LocalDateTime finishedAt = record.getFinishedAt();
        if (startedAt == null || finishedAt == null || finishedAt.isBefore(startedAt)) {
            return;
        }

        Timer.builder("deployment.jobs.execution")
                .tag("provider", tagValue(record.getProvider()))
                .tag("status", finalStatus.name())
                .register(meterRegistry)
                .record(Duration.between(startedAt, finishedAt));
    }

    private String tagValue(CloudProvider provider) {
        return provider == null ? "UNKNOWN" : provider.name();
    }
}
