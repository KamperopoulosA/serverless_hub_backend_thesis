package com.serverless.platformselector.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class TraceHeaderFilter extends OncePerRequestFilter {

    public static final String TRACE_HEADER_NAME = "X-Trace-Id";
    public static final String SPAN_HEADER_NAME = "X-Span-Id";

    private final Tracer tracer;
    private final AppObservabilityProperties observabilityProperties;

    public TraceHeaderFilter(@Nullable Tracer tracer, AppObservabilityProperties observabilityProperties) {
        this.tracer = tracer;
        this.observabilityProperties = observabilityProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestIdHeader = observabilityProperties.getRequestIdHeaderName();
        String requestId = request.getHeader(requestIdHeader);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put(CorrelationIdFilter.REQUEST_ID_MDC_KEY, requestId);
        syncTraceContextToMdc();

        try {
            filterChain.doFilter(request, response);
        } finally {
            syncTraceContextToMdc();
            response.setHeader(requestIdHeader, requestId);
            response.setHeader(TRACE_HEADER_NAME, resolveTraceId());
            response.setHeader(SPAN_HEADER_NAME, resolveSpanId());

            MDC.remove(CorrelationIdFilter.REQUEST_ID_MDC_KEY);
            MDC.remove(CorrelationIdFilter.TRACE_ID_MDC_KEY);
            MDC.remove(CorrelationIdFilter.SPAN_ID_MDC_KEY);
        }
    }

    private void syncTraceContextToMdc() {
        Span currentSpan = tracer == null ? null : tracer.currentSpan();
        if (currentSpan == null) {
            return;
        }

        if (currentSpan.context().traceId() != null) {
            MDC.put(CorrelationIdFilter.TRACE_ID_MDC_KEY, currentSpan.context().traceId());
        }
        if (currentSpan.context().spanId() != null) {
            MDC.put(CorrelationIdFilter.SPAN_ID_MDC_KEY, currentSpan.context().spanId());
        }
    }

    private String resolveTraceId() {
        Span currentSpan = tracer == null ? null : tracer.currentSpan();
        if (currentSpan != null && currentSpan.context().traceId() != null) {
            return currentSpan.context().traceId();
        }

        String mdcTraceId = MDC.get(CorrelationIdFilter.TRACE_ID_MDC_KEY);
        return mdcTraceId == null || mdcTraceId.isBlank() ? "N/A" : mdcTraceId;
    }

    private String resolveSpanId() {
        Span currentSpan = tracer == null ? null : tracer.currentSpan();
        if (currentSpan != null && currentSpan.context().spanId() != null) {
            return currentSpan.context().spanId();
        }

        String mdcSpanId = MDC.get(CorrelationIdFilter.SPAN_ID_MDC_KEY);
        return mdcSpanId == null || mdcSpanId.isBlank()
                ? UUID.randomUUID().toString().replace("-", "")
                : mdcSpanId;
    }
}
