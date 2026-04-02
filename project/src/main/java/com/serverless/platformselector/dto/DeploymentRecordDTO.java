package com.serverless.platformselector.dto;

import com.serverless.platformselector.enums.DeploymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class DeploymentRecordDTO {

    private UUID id;
    private UUID userId;
    private String platformName;
    private String functionName;
    private DeploymentStatus status;
    private String endpointUrl;
    private LocalDateTime createdAt;

    public DeploymentRecordDTO() {}

    public DeploymentRecordDTO(UUID id,
                               UUID userId,
                               String platformName,
                               String functionName,
                               DeploymentStatus status,
                               String endpointUrl,
                               LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.platformName = platformName;
        this.functionName = functionName;
        this.status = status;
        this.endpointUrl = endpointUrl;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public DeploymentStatus getStatus() { return status; }
    public void setStatus(DeploymentStatus status) { this.status = status; }

    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
