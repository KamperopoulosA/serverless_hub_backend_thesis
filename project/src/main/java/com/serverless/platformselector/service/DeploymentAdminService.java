package com.serverless.platformselector.service;

import com.serverless.platformselector.dto.DeploymentRecordDTO;
import com.serverless.platformselector.repository.DeploymentRecordRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeploymentAdminService {

    private final DeploymentRecordRepository deploymentRecordRepository;
    private final DeploymentDtoMapper deploymentDtoMapper;

    public DeploymentAdminService(
            DeploymentRecordRepository deploymentRecordRepository,
            DeploymentDtoMapper deploymentDtoMapper) {
        this.deploymentRecordRepository = deploymentRecordRepository;
        this.deploymentDtoMapper = deploymentDtoMapper;
    }

    @Transactional(readOnly = true)
    public List<DeploymentRecordDTO> getAllDeployments() {
        return deploymentRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
            .stream()
            .map(deploymentDtoMapper::toRecordDto)
            .toList();
    }
}
