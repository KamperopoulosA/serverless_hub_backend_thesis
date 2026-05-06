package com.serverless.platformselector.dto;

import com.serverless.platformselector.enums.CloudProvider;
import com.serverless.platformselector.enums.DeploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "DTO for deployment status/details")
public class DeploymentResultDTO {

    @Schema(description = "Deployment record identifier")
    private UUID deploymentId;

    @Schema(description = "Deployment status")
    private DeploymentStatus status;

    @Schema(description = "Function name")
    private String functionName;

    @Schema(description = "Platform identifier")
    private UUID platformId;

    @Schema(description = "Platform name")
    private String platformName;

    @Schema(description = "Cloud provider")
    private CloudProvider provider;

    @Schema(description = "Deployed function endpoint URL")
    private String endpointUrl;

    @Schema(description = "Deployment logs")
    private String logOutput;

    @Schema(description = "Error message if deployment failed")
    private String errorMessage;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Execution start timestamp")
    private LocalDateTime startedAt;

    @Schema(description = "Execution finish timestamp")
    private LocalDateTime finishedAt;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public UUID getPlatformId() {
        return platformId;
    }

    public void setPlatformId(UUID platformId) {
        this.platformId = platformId;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public CloudProvider getProvider() {
        return provider;
    }

    public void setProvider(CloudProvider provider) {
        this.provider = provider;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getLogOutput() {
        return logOutput;
    }

    public void setLogOutput(String logOutput) {
        this.logOutput = logOutput;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
