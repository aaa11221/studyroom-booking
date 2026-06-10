package com.mango.monitoring;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

@Component
public class MonitoringMetrics {

    private final AtomicLong requestCount = new AtomicLong();
    private final AtomicLong errorCount = new AtomicLong();
    private final AtomicLong totalResponseTimeMillis = new AtomicLong();
    private final AtomicLong maxResponseTimeMillis = new AtomicLong();
    private final AtomicLong activeRequests = new AtomicLong();

    public void requestStarted() {
        activeRequests.incrementAndGet();
    }

    public void requestFinished(int status, long durationMillis) {
        requestCount.incrementAndGet();
        totalResponseTimeMillis.addAndGet(durationMillis);
        updateMax(durationMillis);
        if (status >= 400) {
            errorCount.incrementAndGet();
        }
        activeRequests.decrementAndGet();
    }

    public Map<String, Object> snapshot() {
        long requests = requestCount.get();
        long errors = errorCount.get();
        double averageResponseTime = requests == 0 ? 0 : (double) totalResponseTimeMillis.get() / requests;
        double errorRate = requests == 0 ? 0 : (double) errors / requests;

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("requestCount", requests);
        metrics.put("errorCount", errors);
        metrics.put("errorRate", round(errorRate));
        metrics.put("averageResponseTimeMs", round(averageResponseTime));
        metrics.put("maxResponseTimeMs", maxResponseTimeMillis.get());
        metrics.put("activeRequests", activeRequests.get());
        return metrics;
    }

    private void updateMax(long durationMillis) {
        long current;
        do {
            current = maxResponseTimeMillis.get();
            if (durationMillis <= current) {
                return;
            }
        } while (!maxResponseTimeMillis.compareAndSet(current, durationMillis));
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
