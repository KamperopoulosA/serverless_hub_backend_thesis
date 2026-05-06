package com.serverless.platformselector.config;

import com.serverless.platformselector.enums.CloudProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
@ConfigurationProperties(prefix = "app.deployment")
public class AppDeploymentProperties {

    private String artifactsRoot = "./deployment-artifacts";
    private long maxArtifactSizeBytes = 25 * 1024 * 1024;
    private long jobTimeoutSeconds = 600;
    private int logMaxBytes = 100 * 1024;
    private List<CloudProvider> supportedProviders = new ArrayList<>(List.of(CloudProvider.AWS, CloudProvider.GCP));
    private Worker worker = new Worker();

    public String getArtifactsRoot() {
        return artifactsRoot;
    }

    public void setArtifactsRoot(String artifactsRoot) {
        this.artifactsRoot = artifactsRoot;
    }

    public long getMaxArtifactSizeBytes() {
        return maxArtifactSizeBytes;
    }

    public void setMaxArtifactSizeBytes(long maxArtifactSizeBytes) {
        this.maxArtifactSizeBytes = maxArtifactSizeBytes;
    }

    public long getJobTimeoutSeconds() {
        return jobTimeoutSeconds;
    }

    public void setJobTimeoutSeconds(long jobTimeoutSeconds) {
        this.jobTimeoutSeconds = jobTimeoutSeconds;
    }

    public int getLogMaxBytes() {
        return logMaxBytes;
    }

    public void setLogMaxBytes(int logMaxBytes) {
        this.logMaxBytes = logMaxBytes;
    }

    public List<CloudProvider> getSupportedProviders() {
        return supportedProviders;
    }

    public void setSupportedProviders(List<CloudProvider> supportedProviders) {
        this.supportedProviders = supportedProviders;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public static class Worker {
        private boolean enabled = false;
        private long pollIntervalMs = 5000;
        private String workerId = "worker-default";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getPollIntervalMs() {
            return pollIntervalMs;
        }

        public void setPollIntervalMs(long pollIntervalMs) {
            this.pollIntervalMs = pollIntervalMs;
        }

        public String getWorkerId() {
            return workerId;
        }

        public void setWorkerId(String workerId) {
            this.workerId = workerId;
        }
    }
}
