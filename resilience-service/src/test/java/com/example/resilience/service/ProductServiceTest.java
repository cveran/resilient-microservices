package com.example.resilience.service;

import com.example.resilience.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Test
    void getProduct_returnsProduct() {
        Product product = productService.getProduct(1L);

        assertNotNull(product);
        assertEquals(1L, product.id());
        assertNotNull(product.name());
    }

    @Test
    void getAllProducts_returnsProductList() {
        List<Product> products = productService.getAllProducts();

        assertNotNull(products);
        assertFalse(products.isEmpty());
    }

    @Test
    void fallbackProduct_hasExpectedValues() {
        Product fallback = Product.fallback(99L);

        assertEquals(99L, fallback.id());
        assertEquals("Unavailable", fallback.name());
        assertEquals(0, fallback.stockQuantity());
    }
}
