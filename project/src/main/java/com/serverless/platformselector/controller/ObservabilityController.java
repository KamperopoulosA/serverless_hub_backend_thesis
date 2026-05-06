package com.serverless.platformselector.controller;

import com.serverless.platformselector.enums.DeploymentStatus;
import com.serverless.platformselector.repository.DeploymentRecordRepository;
import com.serverless.platformselector.service.WorkerHeartbeatService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/observability")
public class ObservabilityController {

    private final MeterRegistry meterRegistry;
    private final DeploymentRecordRepository deploymentRecordRepository;
    private final WorkerHeartbeatService workerHeartbeatService;
    private final HealthEndpoint healthEndpoint;

    public ObservabilityController(
            MeterRegistry meterRegistry,
            DeploymentRecordRepository deploymentRecordRepository,
            WorkerHeartbeatService workerHeartbeatService,
            HealthEndpoint healthEndpoint) {
        this.meterRegistry = meterRegistry;
        this.deploymentRecordRepository = deploymentRecordRepository;
        this.workerHeartbeatService = workerHeartbeatService;
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("api", apiSummary());
        result.put("deployments", deploymentSummary());
        result.put("worker", workerHeartbeatService.getWorkerSummary());
        result.put("health", healthSummary());
        return result;
    }

    private Map<String, Object> apiSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("platforms_get", timerSummary(
                meterRegistry.find("http.server.requests")
                        .tag("uri", "/api/platforms")
                        .tag("method", "GET")
                        .timer()
        ));

        summary.put("platforms_search_get", timerSummary(
                meterRegistry.find("http.server.requests")
                        .tag("uri", "/api/platforms/search")
                        .tag("method", "GET")
                        .timer()
        ));

        summary.put("deployments_post", timerSummary(
                meterRegistry.find("http.server.requests")
                        .tag("uri", "/api/deployments")
                        .tag("method", "POST")
                        .timer()
        ));

        summary.put("auth_login_post", timerSummary(
                meterRegistry.find("http.server.requests")
                        .tag("uri", "/api/auth/login")
                        .tag("method", "POST")
                        .timer()
        ));

        return summary;
    }

    private Map<String, Object> deploymentSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("queued", deploymentRecordRepository.countByDeploymentStatus(DeploymentStatus.QUEUED));
        summary.put("running", deploymentRecordRepository.countByDeploymentStatus(DeploymentStatus.RUNNING));
        summary.put("success", deploymentRecordRepository.countByDeploymentStatus(DeploymentStatus.SUCCESS));
        summary.put("failed", deploymentRecordRepository.countByDeploymentStatus(DeploymentStatus.FAILED));
        return summary;
    }

    private Map<String, Object> healthSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        HealthComponent healthComponent = healthEndpoint.health();
        summary.put("apiStatus", healthComponent.getStatus().getCode());

        Map<String, Object> workerSummary = workerHeartbeatService.getWorkerSummary();
        summary.put("workerFresh", workerSummary.get("fresh"));
        summary.put("workerStatus", workerSummary.get("status"));
        return summary;
    }

    private Map<String, Object> timerSummary(Timer timer) {
        Map<String, Object> m = new HashMap<>();
        if (timer == null || timer.count() == 0) {
            m.put("count", 0L);
            m.put("meanMs", 0.0);
            m.put("maxMs", 0.0);
        } else {
            m.put("count", timer.count());

            double meanMs = timer.mean(TimeUnit.MILLISECONDS);
            double maxMs = timer.max(TimeUnit.MILLISECONDS);

            if (Double.isNaN(meanMs)) meanMs = 0.0;
            if (Double.isNaN(maxMs)) maxMs = 0.0;

            m.put("meanMs", meanMs);
            m.put("maxMs", maxMs);
        }
        return m;
    }
}
