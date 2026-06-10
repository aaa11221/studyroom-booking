package com.mango.monitoring;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class MonitoringFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringFilter.class);

    private final MonitoringMetrics monitoringMetrics;

    public MonitoringFilter(MonitoringMetrics monitoringMetrics) {
        this.monitoringMetrics = monitoringMetrics;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        long startedAt = System.currentTimeMillis();
        int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

        monitoringMetrics.requestStarted();
        try {
            chain.doFilter(request, response);
            status = httpResponse.getStatus();
        } catch (IOException | ServletException | RuntimeException e) {
            LOGGER.error("request failed method={} path={}", httpRequest.getMethod(), httpRequest.getRequestURI(), e);
            throw e;
        } finally {
            long durationMillis = System.currentTimeMillis() - startedAt;
            monitoringMetrics.requestFinished(status, durationMillis);
            LOGGER.info("request completed method={} path={} status={} durationMs={}",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    status,
                    durationMillis);
        }
    }
}
