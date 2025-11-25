package com.inditex.similarproducts.api.infrastructure.web;

import com.inditex.similarproducts.api.application.SimilarProductsService;
import com.inditex.similarproducts.api.domain.Product;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@Validated
@Slf4j
public class SimilarProductsController {

    private final SimilarProductsService similarProductsService;

    @GetMapping("/{productId}/similar")
    public ResponseEntity<List<Product>> getSimilarProducts(
            @PathVariable @NotBlank(message = "Product ID is required") String productId) {

        List<Product> products = similarProductsService.getSimilarProducts(productId);
        return ResponseEntity.ok(products);
    }
}

