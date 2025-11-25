package com.example.resilience.controller;

import com.example.resilience.model.ApiResponse;
import com.example.resilience.model.User;
import com.example.resilience.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user operations.
 * 
 * Demonstrates:
 * - Caching with cache management endpoints
 * - Circuit breaker patterns
 * - Graceful degradation
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all users.
     * Results are cached to reduce external service calls.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        log.info("GET /api/v1/users");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Get user by ID.
     * Results are cached per user ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
        log.info("GET /api/v1/users/{}", id);
        User user = userService.getUser(id);
        
        if ("unknown".equals(user.username())) {
            return ResponseEntity.ok(ApiResponse.fallback(user, "External service unavailable"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Evict cache for a specific user.
     * Useful when user data is updated externally.
     */
    @DeleteMapping("/{id}/cache")
    public ResponseEntity<ApiResponse<Void>> evictUserCache(@PathVariable Long id) {
        log.info("DELETE /api/v1/users/{}/cache", id);
        userService.evictUserCache(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Evict all user caches.
     * Useful for cache invalidation during deployments or data migrations.
     */
    @DeleteMapping("/cache")
    public ResponseEntity<ApiResponse<Void>> evictAllUserCache() {
        log.info("DELETE /api/v1/users/cache");
        userService.evictAllUserCache();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
