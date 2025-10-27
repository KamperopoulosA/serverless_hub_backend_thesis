package com.serverless.platformselector.dto;

import com.serverless.platformselector.enums.DeploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "DTO for deployment result")
public class DeploymentResultDTO {
    
    @Schema(description = "Deployment record identifier")
    private UUID deploymentId;
    
    @Schema(description = "Deployment status")
    private DeploymentStatus status;
    
    @Schema(description = "Deployed function endpoint URL")
    private String endpointUrl;
    
    @Schema(description = "Deployment logs")
    private String logs;
    
    @Schema(description = "Error message if deployment failed")
    private String errorMessage;
    
    // Constructors
    public DeploymentResultDTO() {}
    
    public DeploymentResultDTO(UUID deploymentId, DeploymentStatus status, String endpointUrl, String logs) {
        this.deploymentId = deploymentId;
        this.status = status;
        this.endpointUrl = endpointUrl;
        this.logs = logs;
    }

    public DeploymentResultDTO(UUID deploymentId, DeploymentStatus status, String endpointUrl, String logs, String errorMessage) {
        this.deploymentId = deploymentId;
        this.status = status;
        this.endpointUrl = endpointUrl;
        this.logs = logs;
        this.errorMessage = errorMessage;
    }


    // Getters and Setters
    public UUID getDeploymentId() { return deploymentId; }
    public void setDeploymentId(UUID deploymentId) { this.deploymentId = deploymentId; }
    
    public DeploymentStatus getStatus() { return status; }
    public void setStatus(DeploymentStatus status) { this.status = status; }
    
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    
    public String getLogs() { return logs; }
    public void setLogs(String logs) { this.logs = logs; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}