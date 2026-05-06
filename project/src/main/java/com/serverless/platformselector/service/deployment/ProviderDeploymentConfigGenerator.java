package com.serverless.platformselector.service.deployment;

import com.serverless.platformselector.enums.CloudProvider;

public interface ProviderDeploymentConfigGenerator {

    CloudProvider getProvider();

    GeneratedDeploymentConfig generate(DeploymentExecutionContext context) throws Exception;
}
