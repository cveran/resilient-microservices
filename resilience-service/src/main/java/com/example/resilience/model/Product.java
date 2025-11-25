package com.example.resilience.model;

import java.math.BigDecimal;

/**
 * Product model for demonstrating resilience patterns.
 */
public record Product(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stockQuantity
) {
    /**
     * Creates a fallback product when the external service is unavailable.
     */
    public static Product fallback(Long id) {
        return new Product(
                id,
                "Unavailable",
                "Product information temporarily unavailable",
                BigDecimal.ZERO,
                0
        );
    }
}
