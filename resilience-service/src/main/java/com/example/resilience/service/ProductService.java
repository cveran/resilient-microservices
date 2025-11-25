package com.example.resilience.service;

import com.example.resilience.config.CacheConfig;
import com.example.resilience.exception.ServiceUnavailableException;
import com.example.resilience.model.Product;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Product service demonstrating resilience patterns.
 * 
 * Demonstrates:
 * - Circuit breaker pattern for fault tolerance
 * - Retry mechanism for transient failures
 * - Time limiter for timeout handling
 * - Caching for performance optimization
 * - Graceful degradation with fallback responses
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final String CIRCUIT_BREAKER_NAME = "productService";

    private final RestClient restClient;

    public ProductService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Get product by ID with caching and circuit breaker.
     * Falls back to a degraded response if the external service is unavailable.
     */
    @Cacheable(value = CacheConfig.PRODUCTS_CACHE, key = "#id")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getProductFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public Product getProduct(Long id) {
        log.info("Fetching product with id: {}", id);
        
        // Simulate external API call - in production, this would call a real service
        simulateExternalCall();
        
        return new Product(
                id,
                "Product " + id,
                "Description for product " + id,
                BigDecimal.valueOf(99.99),
                100
        );
    }

    /**
     * Get product with timeout handling using TimeLimiter.
     * Returns a CompletableFuture to support async timeout.
     */
    @TimeLimiter(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getProductAsyncFallback")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getProductAsyncFallback")
    public CompletableFuture<Product> getProductAsync(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Async fetching product with id: {}", id);
            simulateExternalCall();
            return new Product(
                    id,
                    "Product " + id,
                    "Async description for product " + id,
                    BigDecimal.valueOf(149.99),
                    50
            );
        });
    }

    /**
     * Get all products with retry mechanism.
     */
    @Cacheable(value = CacheConfig.PRODUCTS_CACHE, key = "'all'")
    @Retry(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getAllProductsFallback")
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        simulateExternalCall();
        
        return List.of(
                new Product(1L, "Laptop", "High-performance laptop", BigDecimal.valueOf(1299.99), 25),
                new Product(2L, "Phone", "Latest smartphone", BigDecimal.valueOf(999.99), 100),
                new Product(3L, "Tablet", "Portable tablet", BigDecimal.valueOf(599.99), 50)
        );
    }

    /**
     * Fallback method for single product retrieval.
     */
    public Product getProductFallback(Long id, Throwable throwable) {
        log.warn("Fallback triggered for product {}: {}", id, throwable.getMessage());
        return Product.fallback(id);
    }

    /**
     * Fallback method for async product retrieval.
     */
    public CompletableFuture<Product> getProductAsyncFallback(Long id, Throwable throwable) {
        log.warn("Async fallback triggered for product {}: {}", id, throwable.getMessage());
        return CompletableFuture.completedFuture(Product.fallback(id));
    }

    /**
     * Fallback method for all products retrieval.
     */
    public List<Product> getAllProductsFallback(Throwable throwable) {
        log.warn("Fallback triggered for all products: {}", throwable.getMessage());
        return List.of(Product.fallback(0L));
    }

    /**
     * Simulate external service call with potential failures.
     * In production, this would be replaced with actual RestClient calls.
     */
    private void simulateExternalCall() {
        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("ProductAPI", "Service interrupted");
        }
    }
}
