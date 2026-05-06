package com.serverless.platformselector.service;

import com.serverless.platformselector.config.AppDeploymentProperties;
import com.serverless.platformselector.dto.DeploymentRequestDTO;
import com.serverless.platformselector.dto.DeploymentResultDTO;
import com.serverless.platformselector.entity.DeploymentRecord;
import com.serverless.platformselector.entity.OurUsers;
import com.serverless.platformselector.entity.Platform;
import com.serverless.platformselector.enums.CloudProvider;
import com.serverless.platformselector.enums.DeploymentStatus;
import com.serverless.platformselector.exception.BadRequestException;
import com.serverless.platformselector.exception.ResourceNotFoundException;
import com.serverless.platformselector.repository.DeploymentRecordRepository;
import com.serverless.platformselector.repository.PlatformRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

@Service
@Transactional
public class DeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentService.class);

    private final DeploymentRecordRepository deploymentRecordRepository;
    private final PlatformRepository platformRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final DeploymentArtifactService deploymentArtifactService;
    private final DeploymentDtoMapper deploymentDtoMapper;
    private final AppDeploymentProperties deploymentProperties;
    private final DeploymentMetricsService deploymentMetricsService;

    public DeploymentService(
            DeploymentRecordRepository deploymentRecordRepository,
            PlatformRepository platformRepository,
            AuthenticatedUserService authenticatedUserService,
            DeploymentArtifactService deploymentArtifactService,
            DeploymentDtoMapper deploymentDtoMapper,
            AppDeploymentProperties deploymentProperties,
            DeploymentMetricsService deploymentMetricsService) {
        this.deploymentRecordRepository = deploymentRecordRepository;
        this.platformRepository = platformRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.deploymentArtifactService = deploymentArtifactService;
        this.deploymentDtoMapper = deploymentDtoMapper;
        this.deploymentProperties = deploymentProperties;
        this.deploymentMetricsService = deploymentMetricsService;
    }

    public DeploymentResultDTO queueDeployment(DeploymentRequestDTO deploymentRequest, Authentication authentication) {
        OurUsers owner = authenticatedUserService.requireCurrentUser(authentication);
        Platform platform = platformRepository.findById(deploymentRequest.getPlatformId())
            .orElseThrow(() -> new ResourceNotFoundException("Platform not found with id: " + deploymentRequest.getPlatformId()));

        CloudProvider provider = platform.getProvider();
        if (provider == null) {
            throw new BadRequestException("Selected platform is missing a configured provider");
        }

        if (!deploymentProperties.getSupportedProviders().contains(provider)) {
            throw new BadRequestException("Deployments are not supported for provider " + provider);
        }

        Path artifactPath = deploymentArtifactService.storeArtifact(
            deploymentRequest.getFunctionPackageBase64(),
            "deployment-" + UUID.randomUUID() + ".zip"
        );

        DeploymentRecord record = new DeploymentRecord();
        record.setUserId(legacyUserId(owner));
        record.setOwnerUserId(owner.getId());
        record.setPlatform(platform);
        record.setProvider(provider);
        record.setFunctionName(deploymentRequest.getFunctionName().trim());
        record.setRuntime(cleanNullable(deploymentRequest.getRuntime()));
        record.setHandler(cleanNullable(deploymentRequest.getHandler()));
        record.setRegion(cleanNullable(deploymentRequest.getRegion()));
        record.setArtifactPath(artifactPath.toString());
        record.setDeploymentStatus(DeploymentStatus.QUEUED);

        DeploymentRecord savedRecord = deploymentRecordRepository.saveAndFlush(record);
        deploymentMetricsService.recordCreated(provider);
        logger.info("Queued deployment job {} for provider {}", savedRecord.getId(), provider);
        return deploymentDtoMapper.toResultDto(savedRecord);
    }

    @Transactional(readOnly = true)
    public DeploymentResultDTO getDeployment(UUID deploymentId, Authentication authentication) {
        return deploymentDtoMapper.toResultDto(findAccessibleDeployment(deploymentId, authentication));
    }

    @Transactional(readOnly = true)
    public DeploymentRecord findAccessibleDeployment(UUID deploymentId, Authentication authentication) {
        if (authenticatedUserService.isAdmin(authentication)) {
            return deploymentRecordRepository.findById(deploymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Deployment not found"));
        }

        OurUsers owner = authenticatedUserService.requireCurrentUser(authentication);
        return deploymentRecordRepository.findByIdAndOwnerUserId(deploymentId, owner.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Deployment not found"));
    }

    private UUID legacyUserId(OurUsers owner) {
        String seed = "legacy-user:" + owner.getId() + ":" + owner.getEmail();
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }

    private String cleanNullable(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
