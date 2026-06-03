package com.xxx.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class LogConfig {
    private static final Logger logger = LoggerFactory.getLogger(LogConfig.class);
    private static final ObjectMapper om = new ObjectMapper();

    public static void info(String msg, String module) {
        Map<String, Object> log = new HashMap<>();
        log.put("time", LocalDateTime.now());
        log.put("level", "INFO");
        log.put("message", msg);
        log.put("module", module);
        try {
            logger.info(om.writeValueAsString(log));
        } catch (JsonProcessingException e) {
            logger.info(msg);
        }
    }
}