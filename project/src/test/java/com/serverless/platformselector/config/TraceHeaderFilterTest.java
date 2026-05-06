package com.serverless.platformselector.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TraceHeaderFilterTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldGenerateRequestIdAndExposeTraceHeaders() throws Exception {
        AppObservabilityProperties properties = new AppObservabilityProperties();
        Tracer tracer = mock(Tracer.class);
        Span span = mock(Span.class);
        TraceContext context = mock(TraceContext.class);

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(context);
        when(context.traceId()).thenReturn("trace-123");
        when(context.spanId()).thenReturn("span-456");

        TraceHeaderFilter filter = new TraceHeaderFilter(tracer, properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> assertNotNull(MDC.get(CorrelationIdFilter.REQUEST_ID_MDC_KEY)));

        assertNotNull(response.getHeader(properties.getRequestIdHeaderName()));
        assertEquals("trace-123", response.getHeader(TraceHeaderFilter.TRACE_HEADER_NAME));
        assertEquals("span-456", response.getHeader(TraceHeaderFilter.SPAN_HEADER_NAME));
        assertFalse(MDC.containsKey(CorrelationIdFilter.REQUEST_ID_MDC_KEY));
    }

    @Test
    void shouldPreserveIncomingRequestId() throws Exception {
        AppObservabilityProperties properties = new AppObservabilityProperties();
        TraceHeaderFilter filter = new TraceHeaderFilter(null, properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(properties.getRequestIdHeaderName(), "incoming-request-id");

        filter.doFilter(request, response, (req, res) -> {
        });

        assertEquals("incoming-request-id", response.getHeader(properties.getRequestIdHeaderName()));
    }
}
