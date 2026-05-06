package com.serverless.platformselector.service;

import com.serverless.platformselector.dto.DeploymentRecordDTO;
import com.serverless.platformselector.dto.DeploymentResultDTO;
import com.serverless.platformselector.entity.DeploymentRecord;
import com.serverless.platformselector.entity.Platform;
import org.springframework.stereotype.Service;

@Service
public class DeploymentDtoMapper {

    public DeploymentResultDTO toResultDto(DeploymentRecord record) {
        DeploymentResultDTO dto = new DeploymentResultDTO();
        dto.setDeploymentId(record.getId());
        dto.setStatus(record.getDeploymentStatus());
        dto.setFunctionName(record.getFunctionName());
        dto.setPlatformId(record.getPlatform() != null ? record.getPlatform().getId() : null);
        dto.setPlatformName(record.getPlatform() != null ? record.getPlatform().getName() : null);
        dto.setProvider(record.getProvider());
        dto.setEndpointUrl(record.getEndpointUrl());
        dto.setLogOutput(record.getLogOutput());
        dto.setErrorMessage(record.getErrorMessage());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setStartedAt(record.getStartedAt());
        dto.setFinishedAt(record.getFinishedAt());
        return dto;
    }

    public DeploymentRecordDTO toRecordDto(DeploymentRecord record) {
        Platform platform = record.getPlatform();

        DeploymentRecordDTO dto = new DeploymentRecordDTO();
        dto.setId(record.getId());
        dto.setOwnerUserId(record.getOwnerUserId());
        dto.setPlatformId(platform != null ? platform.getId() : null);
        dto.setPlatformName(platform != null ? platform.getName() : null);
        dto.setProvider(record.getProvider());
        dto.setFunctionName(record.getFunctionName());
        dto.setStatus(record.getDeploymentStatus());
        dto.setEndpointUrl(record.getEndpointUrl());
        dto.setErrorMessage(record.getErrorMessage());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setStartedAt(record.getStartedAt());
        dto.setFinishedAt(record.getFinishedAt());
        return dto;
    }
}
