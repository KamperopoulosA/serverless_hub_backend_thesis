package com.serverless.platformselector.service.deployment;

import java.util.HashMap;
import java.util.Map;

public class GeneratedDeploymentConfig {

    private final String serverlessYml;
    private final Map<String, String> environment;

    public GeneratedDeploymentConfig(String serverlessYml, Map<String, String> environment) {
        this.serverlessYml = serverlessYml;
        this.environment = new HashMap<>(environment);
    }

    public String getServerlessYml() {
        return serverlessYml;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }
}
