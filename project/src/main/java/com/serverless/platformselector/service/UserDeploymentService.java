package com.serverless.platformselector.service;

import com.serverless.platformselector.dto.DeploymentRecordDTO;
import com.serverless.platformselector.entity.OurUsers;
import com.serverless.platformselector.repository.DeploymentRecordRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserDeploymentService {

    private final DeploymentRecordRepository deploymentRecordRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final DeploymentDtoMapper deploymentDtoMapper;

    public UserDeploymentService(
            DeploymentRecordRepository deploymentRecordRepository,
            AuthenticatedUserService authenticatedUserService,
            DeploymentDtoMapper deploymentDtoMapper) {
        this.deploymentRecordRepository = deploymentRecordRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.deploymentDtoMapper = deploymentDtoMapper;
    }

    @Transactional(readOnly = true)
    public List<DeploymentRecordDTO> getDeploymentsForCurrentUser(Authentication authentication) {
        OurUsers user = authenticatedUserService.requireCurrentUser(authentication);

        return deploymentRecordRepository.findByOwnerUserIdOrderByCreatedAtDesc(user.getId())
            .stream()
            .map(deploymentDtoMapper::toRecordDto)
            .toList();
    }
}
