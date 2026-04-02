package com.serverless.platformselector.service;

import com.serverless.platformselector.dto.DeploymentRecordDTO;
import com.serverless.platformselector.entity.DeploymentRecord;
import com.serverless.platformselector.repository.DeploymentRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserDeploymentService {

    private final DeploymentRecordRepository deploymentRecordRepository;

    public UserDeploymentService(DeploymentRecordRepository deploymentRecordRepository) {
        this.deploymentRecordRepository = deploymentRecordRepository;
    }


    public List<DeploymentRecordDTO> getDeploymentsForUser(UUID userId) {
        return deploymentRecordRepository.findByUserId(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private DeploymentRecordDTO toDto(DeploymentRecord record) {
        DeploymentRecordDTO dto = new DeploymentRecordDTO();
        dto.setId(record.getId());
        dto.setUserId(record.getUserId());
        dto.setPlatformName(
                record.getPlatform() != null ? record.getPlatform().getName() : null
        );
        dto.setFunctionName(record.getFunctionName());
        dto.setStatus(record.getDeploymentStatus());
        dto.setEndpointUrl(record.getEndpointUrl());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }
}
