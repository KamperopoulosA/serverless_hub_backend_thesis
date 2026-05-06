package com.serverless.platformselector.service.deployment;

import com.serverless.platformselector.enums.CloudProvider;
import com.serverless.platformselector.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AwsDeploymentConfigGenerator implements ProviderDeploymentConfigGenerator {

    @Override
    public CloudProvider getProvider() {
        return CloudProvider.AWS;
    }

    @Override
    public GeneratedDeploymentConfig generate(DeploymentExecutionContext context) {
        Map<String, String> credentials = context.getCredentials();
        String accessKey = credentials.get("AWS_ACCESS_KEY_ID");
        String secretKey = credentials.get("AWS_SECRET_ACCESS_KEY");
        if (accessKey == null || secretKey == null) {
            throw new BadRequestException("Missing saved AWS credentials");
        }

        String region = context.getDeploymentRecord().getRegion();
        if (region == null || region.isBlank()) {
            region = credentials.getOrDefault("AWS_REGION", "us-east-1");
        }

        String runtime = defaultValue(context.getDeploymentRecord().getRuntime(), "nodejs18.x");
        String handler = defaultValue(context.getDeploymentRecord().getHandler(), "index.handler");
        String functionName = context.getDeploymentRecord().getFunctionName();

        String serverlessYml = String.join("\n",
            "service: " + sanitize(functionName),
            "frameworkVersion: '3'",
            "provider:",
            "  name: aws",
            "  runtime: " + runtime,
            "  region: " + region,
            "functions:",
            "  " + sanitize(functionName) + ":",
            "    handler: " + handler,
            "    events:",
            "      - http:",
            "          path: /" + sanitize(functionName),
            "          method: get",
            "");

        Map<String, String> environment = new HashMap<>();
        environment.put("AWS_ACCESS_KEY_ID", accessKey);
        environment.put("AWS_SECRET_ACCESS_KEY", secretKey);
        environment.put("AWS_REGION", region);
        return new GeneratedDeploymentConfig(serverlessYml, environment);
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String sanitize(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9-]", "-");
    }
}
