package com.example.resilience.model;

/**
 * User model for demonstrating caching patterns.
 */
public record User(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName
) {
    /**
     * Creates a fallback user when the external service is unavailable.
     */
    public static User fallback(Long id) {
        return new User(
                id,
                "unknown",
                "unavailable@example.com",
                "Unknown",
                "User"
        );
    }
}
