package com.serverless.platformselector.health;

import com.serverless.platformselector.config.AppDeploymentProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Profile("worker")
public class ArtifactsRootHealthIndicator implements HealthIndicator {

    private final AppDeploymentProperties deploymentProperties;

    public ArtifactsRootHealthIndicator(AppDeploymentProperties deploymentProperties) {
        this.deploymentProperties = deploymentProperties;
    }

    @Override
    public Health health() {
        try {
            Path artifactsRoot = Path.of(deploymentProperties.getArtifactsRoot()).toAbsolutePath().normalize();
            Files.createDirectories(artifactsRoot);

            if (!Files.isWritable(artifactsRoot)) {
                return Health.down()
                        .withDetail("artifactsRoot", artifactsRoot.toString())
                        .withDetail("reason", "Artifacts root is not writable")
                        .build();
            }

            return Health.up()
                    .withDetail("artifactsRoot", artifactsRoot.toString())
                    .build();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }
}
