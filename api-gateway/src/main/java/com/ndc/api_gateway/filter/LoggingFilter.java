package com.ndc.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * LoggingFilter - Global filter để log tất cả requests đi qua API Gateway
 * Hữu ích cho monitoring và debugging
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();
        
        // Log incoming request (đơn giản)
        logger.info("API Gateway: {} {} - {}", 
            request.getMethod(), 
            request.getPath().value(),
            request.getRemoteAddress());
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            ServerHttpResponse response = exchange.getResponse();
            
            // Log response (đơn giản)
            logger.info("API Gateway Response: {} - {} ms", 
                response.getStatusCode(), 
                duration);
        }));
    }

    @Override
    public int getOrder() {
        return -1; // Highest priority - execute first
    }
} 