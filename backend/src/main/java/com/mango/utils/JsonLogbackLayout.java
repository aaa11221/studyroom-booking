package com.mango.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.LayoutBase;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonLogbackLayout extends LayoutBase<ILoggingEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String doLayout(ILoggingEvent event) {
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("time", new java.util.Date(event.getTimeStamp()).toInstant().toString());
        log.put("level", event.getLevel().toString());
        log.put("message", event.getFormattedMessage());
        log.put("logger", event.getLoggerName());
        log.put("thread", event.getThreadName());
        log.put("module", moduleName(event.getLoggerName()));

        IThrowableProxy throwable = event.getThrowableProxy();
        if (throwable != null) {
            log.put("exception", throwable.getClassName());
            log.put("exceptionMessage", throwable.getMessage());
            StackTraceElementProxy[] stackTrace = throwable.getStackTraceElementProxyArray();
            if (stackTrace != null && stackTrace.length > 0) {
                log.put("stackTraceTop", stackTrace[0].toString());
            }
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(log) + System.lineSeparator();
        } catch (Exception e) {
            return "{\"level\":\"ERROR\",\"message\":\"failed to serialize log event\"}" + System.lineSeparator();
        }
    }

    private String moduleName(String loggerName) {
        if (loggerName == null || loggerName.isEmpty()) {
            return "";
        }
        int index = loggerName.lastIndexOf('.');
        return index < 0 ? loggerName : loggerName.substring(index + 1);
    }
}
