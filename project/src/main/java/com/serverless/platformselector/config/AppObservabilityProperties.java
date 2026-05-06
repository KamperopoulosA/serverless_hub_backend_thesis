package com.serverless.platformselector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.observability")
public class AppObservabilityProperties {

    private String requestIdHeaderName = "X-Request-Id";
    private long workerHeartbeatStaleAfterSeconds = 30;
    private String serviceName = "serverless-platform-selector-api";

    public String getRequestIdHeaderName() {
        return requestIdHeaderName;
    }

    public void setRequestIdHeaderName(String requestIdHeaderName) {
        this.requestIdHeaderName = requestIdHeaderName;
    }

    public long getWorkerHeartbeatStaleAfterSeconds() {
        return workerHeartbeatStaleAfterSeconds;
    }

    public void setWorkerHeartbeatStaleAfterSeconds(long workerHeartbeatStaleAfterSeconds) {
        this.workerHeartbeatStaleAfterSeconds = workerHeartbeatStaleAfterSeconds;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
