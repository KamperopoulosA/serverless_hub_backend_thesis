package com.serverless.platformselector.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TraceHeaderFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(TraceHeaderFilter.class);
    private final Tracer tracer;

    public TraceHeaderFilter(@Nullable Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Τρέχει πρώτα όλο το υπόλοιπο chain (controller, security κλπ)


        String traceId = null;
        String spanId = null;

        if (tracer != null) {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                traceId = currentSpan.context().traceId();
                spanId = currentSpan.context().spanId();
            } else {
                LOG.debug("TraceHeaderFilter: currentSpan is null");
            }
        } else {
            LOG.debug("TraceHeaderFilter: tracer is null (no micrometer tracing configured)");
        }

        // Fallback: αν δεν βρέθηκε span, φτιάχνουμε ένα pseudo-span id
        if (spanId == null) {
            spanId = UUID.randomUUID().toString().replace("-", "");
        }
        if (traceId == null) {
            traceId = "N/A";
        }

        response.setHeader("X-Trace-Id", traceId);
        response.setHeader("X-Span-Id", spanId);
        filterChain.doFilter(request, response);
    }

}
