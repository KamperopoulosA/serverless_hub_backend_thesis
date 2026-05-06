package com.serverless.platformselector.health;

import com.serverless.platformselector.config.AppDeploymentProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.actuate.health.Health;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArtifactsRootHealthIndicatorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldReportUpWhenArtifactsRootIsWritable() {
        AppDeploymentProperties properties = new AppDeploymentProperties();
        properties.setArtifactsRoot(tempDir.toString());

        ArtifactsRootHealthIndicator indicator = new ArtifactsRootHealthIndicator(properties);
        Health health = indicator.health();

        assertEquals("UP", health.getStatus().getCode());
    }
}
