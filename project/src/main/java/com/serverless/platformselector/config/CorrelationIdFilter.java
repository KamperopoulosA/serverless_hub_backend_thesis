package com.serverless.platformselector.config;

public final class CorrelationIdFilter {

    public static final String REQUEST_ID_MDC_KEY = "requestId";
    public static final String TRACE_ID_MDC_KEY = "traceId";
    public static final String SPAN_ID_MDC_KEY = "spanId";
    public static final String DEPLOYMENT_ID_MDC_KEY = "deploymentId";
    public static final String WORKER_ID_MDC_KEY = "workerId";
    public static final String PROVIDER_MDC_KEY = "provider";

    private CorrelationIdFilter() {
    }
}
