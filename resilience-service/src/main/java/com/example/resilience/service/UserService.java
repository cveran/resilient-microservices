package com.example.resilience.service;

import com.example.resilience.config.CacheConfig;
import com.example.resilience.exception.ServiceUnavailableException;
import com.example.resilience.model.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * User service demonstrating caching and resilience patterns.
 * 
 * Demonstrates:
 * - Cacheable operations
 * - Cache eviction
 * - Circuit breaker integration
 * - Graceful degradation
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String CIRCUIT_BREAKER_NAME = "userService";

    private final RestClient restClient;

    public UserService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Get user by ID with caching.
     * Results are cached to reduce load on external services.
     */
    @Cacheable(value = CacheConfig.USERS_CACHE, key = "#id")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getUserFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public User getUser(Long id) {
        log.info("Fetching user with id: {} (not from cache)", id);
        
        simulateExternalCall();
        
        return new User(
                id,
                "user" + id,
                "user" + id + "@example.com",
                "John",
                "Doe"
        );
    }

    /**
     * Get all users with caching and circuit breaker.
     */
    @Cacheable(value = CacheConfig.USERS_CACHE, key = "'all'")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getAllUsersFallback")
    public List<User> getAllUsers() {
        log.info("Fetching all users (not from cache)");
        
        simulateExternalCall();
        
        return List.of(
                new User(1L, "alice", "alice@example.com", "Alice", "Smith"),
                new User(2L, "bob", "bob@example.com", "Bob", "Johnson"),
                new User(3L, "carol", "carol@example.com", "Carol", "Williams")
        );
    }

    /**
     * Clear user cache for a specific user.
     * Useful when user data is updated.
     */
    @CacheEvict(value = CacheConfig.USERS_CACHE, key = "#id")
    public void evictUserCache(Long id) {
        log.info("Evicting cache for user: {}", id);
    }

    /**
     * Clear all user caches.
     */
    @CacheEvict(value = CacheConfig.USERS_CACHE, allEntries = true)
    public void evictAllUserCache() {
        log.info("Evicting all user caches");
    }

    /**
     * Fallback method for single user retrieval.
     */
    public User getUserFallback(Long id, Throwable throwable) {
        log.warn("Fallback triggered for user {}: {}", id, throwable.getMessage());
        return User.fallback(id);
    }

    /**
     * Fallback method for all users retrieval.
     */
    public List<User> getAllUsersFallback(Throwable throwable) {
        log.warn("Fallback triggered for all users: {}", throwable.getMessage());
        return List.of(User.fallback(0L));
    }

    /**
     * Simulate external service call.
     */
    private void simulateExternalCall() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("UserAPI", "Service interrupted");
        }
    }
}
