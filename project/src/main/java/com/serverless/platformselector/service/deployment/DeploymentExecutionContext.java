package com.serverless.platformselector.service.deployment;

import com.serverless.platformselector.entity.DeploymentRecord;

import java.nio.file.Path;
import java.util.Map;

public class DeploymentExecutionContext {

    private final DeploymentRecord deploymentRecord;
    private final Path workspacePath;
    private final Map<String, String> credentials;

    public DeploymentExecutionContext(DeploymentRecord deploymentRecord, Path workspacePath, Map<String, String> credentials) {
        this.deploymentRecord = deploymentRecord;
        this.workspacePath = workspacePath;
        this.credentials = credentials;
    }

    public DeploymentRecord getDeploymentRecord() {
        return deploymentRecord;
    }

    public Path getWorkspacePath() {
        return workspacePath;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }
}
