package com.mango.control;

import com.mango.monitoring.MonitoringMetrics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MonitoringMetrics monitoringMetrics;

    @Test
    void healthReturnsStatusTimestampVersionAndRequestCount() throws Exception {
        when(monitoringMetrics.snapshot()).thenReturn(metrics());

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("healthy")))
                .andExpect(jsonPath("$.version", is("1.0.0")))
                .andExpect(jsonPath("$.requests", is(3)))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void metricsReturnsCurrentCounters() throws Exception {
        when(monitoringMetrics.snapshot()).thenReturn(metrics());

        mockMvc.perform(get("/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestCount", is(3)))
                .andExpect(jsonPath("$.errorCount", is(1)))
                .andExpect(jsonPath("$.errorRate", is(0.333)))
                .andExpect(jsonPath("$.averageResponseTimeMs", is(12.5)))
                .andExpect(jsonPath("$.activeRequests", is(0)));
    }

    private Map<String, Object> metrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("requestCount", 3L);
        metrics.put("errorCount", 1L);
        metrics.put("errorRate", 0.333);
        metrics.put("averageResponseTimeMs", 12.5);
        metrics.put("maxResponseTimeMs", 25L);
        metrics.put("activeRequests", 0L);
        return metrics;
    }
}
