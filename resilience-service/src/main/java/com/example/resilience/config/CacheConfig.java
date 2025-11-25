package com.example.resilience.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Caffeine cache.
 * 
 * Demonstrates high-performance caching for:
 * - Reducing external API calls
 * - Improving response times
 * - Handling high-traffic scenarios
 */
@Configuration
public class CacheConfig {

    public static final String PRODUCTS_CACHE = "products";
    public static final String USERS_CACHE = "users";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineCacheBuilder());
        cacheManager.setCacheNames(java.util.List.of(PRODUCTS_CACHE, USERS_CACHE));
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats();
    }
}
