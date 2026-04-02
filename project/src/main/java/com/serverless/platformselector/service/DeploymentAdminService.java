package com.serverless.platformselector.service;

import com.serverless.platformselector.dto.DeploymentRecordDTO;
import com.serverless.platformselector.entity.DeploymentRecord;
import com.serverless.platformselector.repository.DeploymentRecordRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeploymentAdminService {

    private final DeploymentRecordRepository deploymentRecordRepository;

    public DeploymentAdminService(DeploymentRecordRepository deploymentRecordRepository) {
        this.deploymentRecordRepository = deploymentRecordRepository;
    }

    public List<DeploymentRecordDTO> getAllDeployments() {
        // Αν θες τα πιο πρόσφατα πρώτα:
        List<DeploymentRecord> records =
                deploymentRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        return records.stream()
                .map(dr -> new DeploymentRecordDTO(
                        dr.getId(),
                        dr.getUserId(),
                        dr.getPlatform() != null ? dr.getPlatform().getName() : null,
                        dr.getFunctionName(),
                        dr.getDeploymentStatus(),
                        dr.getEndpointUrl(),
                        dr.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
