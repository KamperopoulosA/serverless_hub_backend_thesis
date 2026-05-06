package com.serverless.platformselector.service;

import com.serverless.platformselector.config.AppDeploymentProperties;
import com.serverless.platformselector.config.CorrelationIdFilter;
import com.serverless.platformselector.entity.DeploymentRecord;
import com.serverless.platformselector.enums.CloudProvider;
import com.serverless.platformselector.enums.DeploymentStatus;
import com.serverless.platformselector.exception.BadRequestException;
import com.serverless.platformselector.repository.DeploymentRecordRepository;
import com.serverless.platformselector.service.deployment.CommandResult;
import com.serverless.platformselector.service.deployment.DeploymentCommandRunner;
import com.serverless.platformselector.service.deployment.DeploymentExecutionContext;
import com.serverless.platformselector.service.deployment.GeneratedDeploymentConfig;
import com.serverless.platformselector.service.deployment.ProviderDeploymentConfigGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DeploymentWorkerService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentWorkerService.class);
    private static final Pattern ENDPOINT_PATTERN = Pattern.compile("https://[A-Za-z0-9./?=_:%-]+");

    private final DeploymentRecordRepository deploymentRecordRepository;
    private final ProviderCredentialsService providerCredentialsService;
    private final DeploymentArtifactService deploymentArtifactService;
    private final DeploymentCommandRunner deploymentCommandRunner;
    private final Map<CloudProvider, ProviderDeploymentConfigGenerator> generatorsByProvider;
    private final AppDeploymentProperties deploymentProperties;
    private final DeploymentMetricsService deploymentMetricsService;
    private final WorkerHeartbeatService workerHeartbeatService;

    public DeploymentWorkerService(
            DeploymentRecordRepository deploymentRecordRepository,
            ProviderCredentialsService providerCredentialsService,
            DeploymentArtifactService deploymentArtifactService,
            DeploymentCommandRunner deploymentCommandRunner,
            List<ProviderDeploymentConfigGenerator> configGenerators,
            AppDeploymentProperties deploymentProperties,
            DeploymentMetricsService deploymentMetricsService,
            WorkerHeartbeatService workerHeartbeatService) {
        this.deploymentRecordRepository = deploymentRecordRepository;
        this.providerCredentialsService = providerCredentialsService;
        this.deploymentArtifactService = deploymentArtifactService;
        this.deploymentCommandRunner = deploymentCommandRunner;
        this.deploymentProperties = deploymentProperties;
        this.deploymentMetricsService = deploymentMetricsService;
        this.workerHeartbeatService = workerHeartbeatService;
        this.generatorsByProvider = new HashMap<>();
        configGenerators.forEach(generator -> this.generatorsByProvider.put(generator.getProvider(), generator));
    }

    public boolean processNextQueuedJob() {
        Optional<DeploymentRecord> queuedJob =
            deploymentRecordRepository.claimNextQueuedJob(deploymentProperties.getWorker().getWorkerId());

        if (queuedJob.isEmpty()) {
            return false;
        }

        processClaimedJob(queuedJob.get());
        return true;
    }

    public void processClaimedJob(DeploymentRecord record) {
        Path workspacePath = null;
        Path artifactPath = record.getArtifactPath() == null ? null : Path.of(record.getArtifactPath());

        putWorkerContext(record);
        try {
            validateProvider(record.getProvider());
            if (record.getOwnerUserId() == null) {
                throw new BadRequestException("Deployment record is missing an owner");
            }

            logger.info("Starting deployment job {} for provider {}", record.getId(), record.getProvider());
            workerHeartbeatService.recordJobStarted(record.getId());
            deploymentMetricsService.recordStarted(record);

            Map<String, String> credentials = providerCredentialsService.requireCredentials(
                record.getOwnerUserId(),
                record.getProvider()
            );

            workspacePath = deploymentArtifactService.createWorkspace(record.getId().toString());
            if (artifactPath == null || Files.notExists(artifactPath)) {
                throw new BadRequestException("Deployment artifact could not be found");
            }

            deploymentArtifactService.extractZip(artifactPath, workspacePath);
            GeneratedDeploymentConfig generatedConfig = resolveGenerator(record.getProvider())
                .generate(new DeploymentExecutionContext(record, workspacePath, credentials));

            Files.writeString(workspacePath.resolve("serverless.yml"), generatedConfig.getServerlessYml());

            Map<String, String> environment = new HashMap<>(System.getenv());
            environment.putAll(generatedConfig.getEnvironment());

            CommandResult result = deploymentCommandRunner.run(
                workspacePath,
                List.of("serverless", "deploy", "--verbose"),
                environment,
                Duration.ofSeconds(deploymentProperties.getJobTimeoutSeconds())
            );

            record.setLogOutput(deploymentArtifactService.truncateLog(result.getOutput()));
            if (result.getExitCode() != 0) {
                markFailed(record, "Deployment failed with exit code " + result.getExitCode());
            } else {
                record.setDeploymentStatus(DeploymentStatus.SUCCESS);
                record.setErrorMessage(null);
                record.setEndpointUrl(parseEndpointUrl(result.getOutput()));
            }
        } catch (Exception ex) {
            logger.error("Deployment job {} failed", record.getId(), ex);
            if (record.getLogOutput() == null) {
                record.setLogOutput(deploymentArtifactService.truncateLog(ex.getMessage()));
            }
            markFailed(record, ex.getMessage());
        } finally {
            record.setFinishedAt(LocalDateTime.now());
            record.setArtifactPath(null);
            deploymentRecordRepository.saveAndFlush(record);
            if (record.getDeploymentStatus() == DeploymentStatus.SUCCESS) {
                deploymentMetricsService.recordCompleted(record);
                workerHeartbeatService.recordJobCompleted(record.getId());
                logger.info("Deployment job {} completed successfully", record.getId());
            } else if (record.getDeploymentStatus() == DeploymentStatus.FAILED) {
                deploymentMetricsService.recordFailed(record);
                workerHeartbeatService.recordJobFailed(record.getId(), record.getErrorMessage());
                logger.info("Deployment job {} finished with failure", record.getId());
            } else {
                workerHeartbeatService.recordIdleHeartbeat();
            }
            deploymentArtifactService.cleanup(workspacePath);
            deploymentArtifactService.cleanup(artifactPath);
            clearWorkerContext();
        }
    }

    private ProviderDeploymentConfigGenerator resolveGenerator(CloudProvider provider) {
        ProviderDeploymentConfigGenerator generator = generatorsByProvider.get(provider);
        if (generator == null) {
            throw new BadRequestException("No deployment generator configured for provider " + provider);
        }
        return generator;
    }

    private void validateProvider(CloudProvider provider) {
        if (provider == null) {
            throw new BadRequestException("Deployment record is missing a provider");
        }

        if (!deploymentProperties.getSupportedProviders().contains(provider)) {
            throw new BadRequestException("Deployments are not supported for provider " + provider);
        }
    }

    private void markFailed(DeploymentRecord record, String message) {
        record.setDeploymentStatus(DeploymentStatus.FAILED);
        record.setErrorMessage(message);
        record.setEndpointUrl(null);
    }

    private String parseEndpointUrl(String output) {
        if (output == null || output.isBlank()) {
            return null;
        }

        Matcher matcher = ENDPOINT_PATTERN.matcher(output);
        return matcher.find() ? matcher.group() : null;
    }

    private void putWorkerContext(DeploymentRecord record) {
        MDC.put(CorrelationIdFilter.DEPLOYMENT_ID_MDC_KEY, record.getId().toString());
        MDC.put(CorrelationIdFilter.WORKER_ID_MDC_KEY, deploymentProperties.getWorker().getWorkerId());
        MDC.put(CorrelationIdFilter.PROVIDER_MDC_KEY, record.getProvider() == null ? "UNKNOWN" : record.getProvider().name());
    }

    private void clearWorkerContext() {
        MDC.remove(CorrelationIdFilter.DEPLOYMENT_ID_MDC_KEY);
        MDC.remove(CorrelationIdFilter.WORKER_ID_MDC_KEY);
        MDC.remove(CorrelationIdFilter.PROVIDER_MDC_KEY);
    }
}
