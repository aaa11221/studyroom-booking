package com.mango.control;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mango.monitoring.MonitoringMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final MonitoringMetrics monitoringMetrics;

    @Value("${app.version:1.0.0}")
    private String version;

    public HealthController(MonitoringMetrics monitoringMetrics) {
        this.monitoringMetrics = monitoringMetrics;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "healthy");
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("version", version);
        body.put("requests", monitoringMetrics.snapshot().get("requestCount"));
        return body;
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return monitoringMetrics.snapshot();
    }
}
