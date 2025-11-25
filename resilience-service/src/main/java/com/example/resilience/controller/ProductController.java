package com.example.resilience.controller;

import com.example.resilience.model.ApiResponse;
import com.example.resilience.model.Product;
import com.example.resilience.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for product operations.
 * 
 * Demonstrates:
 * - Synchronous endpoints with caching
 * - Asynchronous endpoints with timeout handling
 * - Graceful error handling
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get all products.
     * Results are cached for improved performance.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        log.info("GET /api/v1/products");
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * Get product by ID (synchronous with caching and circuit breaker).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProduct(@PathVariable Long id) {
        log.info("GET /api/v1/products/{}", id);
        Product product = productService.getProduct(id);
        
        // Check if this is a fallback response
        if ("Unavailable".equals(product.name())) {
            return ResponseEntity.ok(ApiResponse.fallback(product, "External service unavailable"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    /**
     * Get product by ID (asynchronous with timeout handling).
     * Uses CompletableFuture for non-blocking operation with time limits.
     */
    @GetMapping("/{id}/async")
    public CompletableFuture<ResponseEntity<ApiResponse<Product>>> getProductAsync(@PathVariable Long id) {
        log.info("GET /api/v1/products/{}/async", id);
        return productService.getProductAsync(id)
                .thenApply(product -> {
                    if ("Unavailable".equals(product.name())) {
                        return ResponseEntity.ok(ApiResponse.fallback(product, "Request timed out"));
                    }
                    return ResponseEntity.ok(ApiResponse.success(product));
                });
    }
}
