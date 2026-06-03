package com.xxx.controller;

import com.xxx.config.LogConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "healthy");
        res.put("timestamp", LocalDateTime.now());
        res.put("version", "1.0.0");
        LogConfig.info("健康检查正常", "health");
        return res;
    }
}