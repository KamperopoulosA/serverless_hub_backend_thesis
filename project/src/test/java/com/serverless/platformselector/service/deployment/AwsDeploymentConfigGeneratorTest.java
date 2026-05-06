package com.serverless.platformselector.service.deployment;

import com.serverless.platformselector.entity.DeploymentRecord;
import com.serverless.platformselector.enums.CloudProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AwsDeploymentConfigGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void generateBuildsAwsConfigAndEnvironment() {
        DeploymentRecord record = new DeploymentRecord();
        record.setProvider(CloudProvider.AWS);
        record.setFunctionName("hello-world");
        record.setRuntime("nodejs20.x");
        record.setHandler("index.handler");
        record.setRegion("eu-central-1");

        DeploymentExecutionContext context = new DeploymentExecutionContext(
            record,
            tempDir,
            Map.of(
                "AWS_ACCESS_KEY_ID", "test-access",
                "AWS_SECRET_ACCESS_KEY", "test-secret"
            )
        );

        AwsDeploymentConfigGenerator generator = new AwsDeploymentConfigGenerator();
        GeneratedDeploymentConfig config = generator.generate(context);

        assertTrue(config.getServerlessYml().contains("name: aws"));
        assertTrue(config.getServerlessYml().contains("runtime: nodejs20.x"));
        assertTrue(config.getServerlessYml().contains("region: eu-central-1"));
        assertEquals("test-access", config.getEnvironment().get("AWS_ACCESS_KEY_ID"));
        assertEquals("test-secret", config.getEnvironment().get("AWS_SECRET_ACCESS_KEY"));
        assertEquals("eu-central-1", config.getEnvironment().get("AWS_REGION"));
    }
}
