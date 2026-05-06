package com.serverless.platformselector.service.deployment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.platformselector.enums.CloudProvider;
import com.serverless.platformselector.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class GcpDeploymentConfigGenerator implements ProviderDeploymentConfigGenerator {

    private final ObjectMapper objectMapper;

    public GcpDeploymentConfigGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CloudProvider getProvider() {
        return CloudProvider.GCP;
    }

    @Override
    public GeneratedDeploymentConfig generate(DeploymentExecutionContext context) throws Exception {
        String credentialsJson = context.getCredentials().get("GCP_SERVICE_ACCOUNT_JSON");
        if (credentialsJson == null || credentialsJson.isBlank()) {
            throw new BadRequestException("Missing saved GCP credentials");
        }

        JsonNode credentialsNode = objectMapper.readTree(credentialsJson);
        String projectId = credentialsNode.path("project_id").asText();
        if (projectId == null || projectId.isBlank()) {
            throw new BadRequestException("GCP credentials must include project_id");
        }

        Path credentialsPath = context.getWorkspacePath().resolve("gcp-service-account.json");
        Files.writeString(credentialsPath, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(credentialsNode));

        String runtime = defaultValue(context.getDeploymentRecord().getRuntime(), "nodejs18");
        String handler = defaultValue(context.getDeploymentRecord().getHandler(), "handler");
        String region = defaultValue(context.getDeploymentRecord().getRegion(), "us-central1");
        String functionName = context.getDeploymentRecord().getFunctionName();

        String serverlessYml = String.join("\n",
            "service: " + sanitize(functionName),
            "frameworkVersion: '3'",
            "provider:",
            "  name: google",
            "  runtime: " + runtime,
            "  region: " + region,
            "  project: " + projectId,
            "  credentials: " + credentialsPath.toAbsolutePath().normalize(),
            "plugins:",
            "  - serverless-google-cloudfunctions",
            "functions:",
            "  " + sanitize(functionName) + ":",
            "    handler: " + handler,
            "    events:",
            "      - http: true",
            "");

        return new GeneratedDeploymentConfig(serverlessYml, new HashMap<>());
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String sanitize(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9-]", "-");
    }
}
