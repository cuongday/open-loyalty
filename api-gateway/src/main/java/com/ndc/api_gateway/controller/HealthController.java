package com.ndc.api_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * HealthController - Kiểm tra trạng thái API Gateway
 */
@RestController
@RequestMapping("/")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "API Gateway");
        response.put("status", "UP");
        response.put("message", "API Gateway đang hoạt động bình thường");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("components", Map.of(
            "gateway", "UP",
            "eureka", "UP",
            "circuitBreaker", "UP"
        ));
        
        return ResponseEntity.ok(response);
    }
} 