package com.serverless.platformselector.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/observability")
public class ObservabilityController {

    private final MeterRegistry meterRegistry;

    public ObservabilityController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        Map<String, Object> result = new HashMap<>();

        // Platforms list (GET /api/platforms)
        result.put("platforms_get", timerSummary(
                meterRegistry.find("http.server.requests")
                        .tag("uri", "/api/platforms")
                        .tag("method", "GET")
                        .timer()
        ));

        // Platforms search (GET /api/platforms/search)
        result.put("platforms_search_get", timerSummary(
                meterRegistry.find("http.server.requests")
                        .tag("uri", "/api/platforms/search")
                        .tag("method", "GET")
                        .timer()
        ));

        // Deployments (POST /api/deployments)
        result.put("deployments_post", timerSummary(
                meterRegistry.find("http.server.requests")
                        .tag("uri", "/api/deployments")
                        .tag("method", "POST")
                        .timer()
        ));

        // Auth login (POST /api/auth/login)
        result.put("auth_login_post", timerSummary(
                meterRegistry.find("http.server.requests")
                        .tag("uri", "/api/auth/login")
                        .tag("method", "POST")
                        .timer()
        ));

        return result;
    }

    private Map<String, Object> timerSummary(Timer timer) {
        Map<String, Object> m = new HashMap<>();
        if (timer == null || timer.count() == 0) {
            m.put("count", 0L);
            m.put("meanMs", 0.0);
            m.put("maxMs", 0.0);
        } else {
            m.put("count", timer.count());

            double meanMs = timer.mean(TimeUnit.MILLISECONDS);
            double maxMs = timer.max(TimeUnit.MILLISECONDS);

            if (Double.isNaN(meanMs)) meanMs = 0.0;
            if (Double.isNaN(maxMs)) maxMs = 0.0;

            m.put("meanMs", meanMs);
            m.put("maxMs", maxMs);
        }
        return m;
    }
}
