package com.example.resilience.controller;

import com.example.resilience.model.Product;
import com.example.resilience.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void getProduct_returnsProduct() throws Exception {
        Product product = new Product(1L, "Test Product", "Description", BigDecimal.valueOf(99.99), 10);
        when(productService.getProduct(1L)).thenReturn(product);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    void getProduct_returnsFallback_whenServiceDegraded() throws Exception {
        Product fallback = Product.fallback(1L);
        when(productService.getProduct(1L)).thenReturn(fallback);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEGRADED"))
                .andExpect(jsonPath("$.data.name").value("Unavailable"));
    }

    @Test
    void getAllProducts_returnsProductList() throws Exception {
        List<Product> products = List.of(
                new Product(1L, "Product 1", "Desc 1", BigDecimal.valueOf(10.00), 5),
                new Product(2L, "Product 2", "Desc 2", BigDecimal.valueOf(20.00), 10)
        );
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}
