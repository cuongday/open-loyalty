package com.ndc.loyalty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Loyalty Service Application - CQRS + Event Sourcing với Axon Framework
 * 
 * Features:
 * - CQRS (Command Query Responsibility Segregation)
 * - Event Sourcing với Axon Framework
 * - Kafka Event Streaming
 * - Member Management
 * - Tier Management
 * - Wallet Management
 * - Transaction Processing
 * - Saga Orchestration
 * 
 * @author NDC Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableKafka
public class LoyaltyServiceApplication {

    public static void main(String[] args) {
        // Set system properties for better performance
        System.setProperty("spring.jpa.properties.hibernate.jdbc.batch_size", "50");
        System.setProperty("spring.jpa.properties.hibernate.order_inserts", "true");
        System.setProperty("spring.jpa.properties.hibernate.order_updates", "true");
        System.setProperty("spring.jpa.properties.hibernate.jdbc.batch_versioned_data", "true");
        
        // Run application
        SpringApplication.run(LoyaltyServiceApplication.class, args);
    }
}