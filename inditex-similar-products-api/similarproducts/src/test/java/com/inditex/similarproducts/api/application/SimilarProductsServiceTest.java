package com.inditex.similarproducts.api.application;

import com.inditex.similarproducts.api.domain.ExternalServiceException;
import com.inditex.similarproducts.api.domain.Product;
import com.inditex.similarproducts.api.domain.ProductNotFoundException;
import com.inditex.similarproducts.api.infrastructure.client.SimulatedProductClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimilarProductsServiceTest {

    @Mock
    private SimulatedProductClient simulatedProductClient;

    @InjectMocks
    private SimilarProductsService similarProductsService;

    @Test
    void shouldReturnAllSimilarProductsWhenAllAreValid() {
        // GIVEN: productId "1" tiene similares "2", "3", "4" y todos son válidos
        String productId = "1";
        List<String> similarIds = List.of("2", "3", "4");

        Product product2 = new Product("2", "Dress", 19.99, true);
        Product product3 = new Product("3", "Blazer", 29.99, false);
        Product product4 = new Product("4", "Boots", 39.99, true);

        when(simulatedProductClient.loadSimilarProductIds(productId)).thenReturn(similarIds);

        when(simulatedProductClient.loadProductDetailForId("2")).thenReturn(product2);
        when(simulatedProductClient.loadProductDetailForId("3")).thenReturn(product3);
        when(simulatedProductClient.loadProductDetailForId("4")).thenReturn(product4);

        // WHEN: obtenemos los productos similares
        List<Product> result = similarProductsService.getSimilarProducts(productId);

        // THEN: devuelve los 3 productos sin filtrar ninguno
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Product::id).containsExactlyInAnyOrder("2", "3", "4");
        assertThat(result).extracting(Product::name).containsExactlyInAnyOrder("Dress", "Blazer", "Boots");

        verify(simulatedProductClient).loadSimilarProductIds(productId);
        verify(simulatedProductClient).loadProductDetailForId("2");
        verify(simulatedProductClient).loadProductDetailForId("3");
        verify(simulatedProductClient).loadProductDetailForId("4");
    }

    @Test
    void shouldFilterOutProductsNotFound() {
        // GIVEN: productId "4" tiene similares ["1","2","5"] pero "5" no existe (404)
        String productId = "4";
        List<String> similarIds = List.of("1", "2", "5");

        Product product1 = new Product("1", "Shirt", 9.99, true);
        Product product2 = new Product("2", "Dress", 19.99, true);

        when(simulatedProductClient.loadSimilarProductIds(productId)).thenReturn(similarIds);

        when(simulatedProductClient.loadProductDetailForId("1")).thenReturn(product1);
        when(simulatedProductClient.loadProductDetailForId("2")).thenReturn(product2);
        when(simulatedProductClient.loadProductDetailForId("5")).thenThrow(new ProductNotFoundException("5"));

        // WHEN: obtenemos los productos similares
        List<Product> result = similarProductsService.getSimilarProducts(productId);

        // THEN: devuelve solo los productos 1 y 2, filtra el 5
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::id).containsExactlyInAnyOrder("1", "2");
        assertThat(result).extracting(Product::id).doesNotContain("5");

        verify(simulatedProductClient).loadSimilarProductIds(productId);
        verify(simulatedProductClient).loadProductDetailForId("1");
        verify(simulatedProductClient).loadProductDetailForId("2");
        verify(simulatedProductClient).loadProductDetailForId("5");
    }

    @Test
    void shouldFilterOutProductsWithExternalServiceError() {
        // GIVEN: productId "5" tiene similares ["1","2","6"] pero "6" da 500 error
        String productId = "5";
        List<String> similarIds = List.of("1", "2", "6");

        Product product1 = new Product("1", "Shirt", 9.99, true);
        Product product2 = new Product("2", "Dress", 19.99, true);

        when(simulatedProductClient.loadSimilarProductIds(productId)).thenReturn(similarIds);
        when(simulatedProductClient.loadProductDetailForId("1")).thenReturn(product1);
        when(simulatedProductClient.loadProductDetailForId("2")).thenReturn(product2);
        when(simulatedProductClient.loadProductDetailForId("6")).
                thenThrow(new ExternalServiceException("Error calling simulado product 6"));

        // WHEN: obtenemos los productos similares
        List<Product> result = similarProductsService.getSimilarProducts(productId);

        // THEN: devuelve solo los productos 1 y 2, filtra el 6 con error
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::id).containsExactlyInAnyOrder("1", "2");
        assertThat(result).extracting(Product::id).doesNotContain("6");

        verify(simulatedProductClient).loadSimilarProductIds(productId);
        verify(simulatedProductClient).loadProductDetailForId("1");
        verify(simulatedProductClient).loadProductDetailForId("2");
        verify(simulatedProductClient).loadProductDetailForId("6");
    }

    @Test
    void shouldReturnEmptyListWhenNoSimilarProducts() {
        // GIVEN: productId "6" no tiene similares (lista vacía)
        String productId = "6";
        List<String> similarIds = Collections.emptyList();

        when(simulatedProductClient.loadSimilarProductIds(productId)).thenReturn(similarIds);

        // WHEN: obtenemos los productos similares
        List<Product> result = similarProductsService.getSimilarProducts(productId);

        // THEN: devuelve lista vacía sin intentar cargar detalles
        assertThat(result).isEmpty();

        verify(simulatedProductClient).loadSimilarProductIds(productId);
        verify(simulatedProductClient, never()).loadProductDetailForId(anyString());
    }

    @Test
    void shouldFilterOutTimeoutProductAndReturnOnlyValidOnes_ProductId3() {
        // GIVEN: productId "3" tiene similares ["100","1000","10000"] según el mock real
        //        "10000" falla por timeout (ExternalServiceException)
        String productId = "3";
        List<String> similarIds = List.of("100", "1000", "10000");

        Product product100 = new Product("100", "Product100", 100.0, true);
        Product product1000 = new Product("1000", "Product1000", 1000.0, false);

        when(simulatedProductClient.loadSimilarProductIds(productId)).thenReturn(similarIds);
        when(simulatedProductClient.loadProductDetailForId("100")).thenReturn(product100);
        when(simulatedProductClient.loadProductDetailForId("1000")).thenReturn(product1000);
        when(simulatedProductClient.loadProductDetailForId("10000"))
                .thenThrow(new ExternalServiceException("Timeout/error calling simulado product 10000"));

        // WHEN: obtenemos los productos similares
        List<Product> result = similarProductsService.getSimilarProducts(productId);

        // THEN: devuelve solo 100 y 1000 (sin 10000 que da timeout)
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::id).containsExactlyInAnyOrder("100", "1000");
        assertThat(result).extracting(Product::id).doesNotContain("10000");

        verify(simulatedProductClient).loadSimilarProductIds(productId);
        verify(simulatedProductClient).loadProductDetailForId("100");
        verify(simulatedProductClient).loadProductDetailForId("1000");
        verify(simulatedProductClient).loadProductDetailForId("10000");
    }
}

