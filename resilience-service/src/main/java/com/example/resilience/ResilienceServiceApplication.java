package com.example.resilience;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Resilience Service Application
 * 
 * A microservice demonstrating resilience patterns:
 * - Timeout handling with RestClient
 * - Graceful degradation (fallback responses)
 * - Caching for performance optimization
 * - Error handling with proper exception management
 * - Performance tuning configurations
 */
@SpringBootApplication
@EnableCaching
public class ResilienceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResilienceServiceApplication.class, args);
    }
}
