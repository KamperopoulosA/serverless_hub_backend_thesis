package com.serverless.platformselector.service;

import com.serverless.platformselector.config.AppDeploymentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("worker")
public class DeploymentWorkerScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentWorkerScheduler.class);

    private final DeploymentWorkerService deploymentWorkerService;
    private final AppDeploymentProperties deploymentProperties;
    private final WorkerHeartbeatService workerHeartbeatService;

    public DeploymentWorkerScheduler(
            DeploymentWorkerService deploymentWorkerService,
            AppDeploymentProperties deploymentProperties,
            WorkerHeartbeatService workerHeartbeatService) {
        this.deploymentWorkerService = deploymentWorkerService;
        this.deploymentProperties = deploymentProperties;
        this.workerHeartbeatService = workerHeartbeatService;
    }

    @Scheduled(fixedDelayString = "${app.deployment.worker.poll-interval-ms:5000}")
    public void processQueue() {
        if (!deploymentProperties.getWorker().isEnabled()) {
            return;
        }

        workerHeartbeatService.recordIdleHeartbeat();
        boolean processed = deploymentWorkerService.processNextQueuedJob();
        if (processed) {
            logger.info("Worker {} processed a deployment job", deploymentProperties.getWorker().getWorkerId());
        }
    }
}
