package com.inditex.similarproducts.api.infrastructure.client;

import com.inditex.similarproducts.api.domain.ExternalServiceException;
import com.inditex.similarproducts.api.domain.Product;
import com.inditex.similarproducts.api.domain.ProductNotFoundException;
import com.inditex.similarproducts.api.infrastructure.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SimulatedProductClient {

    private final RestClient simuladoRestClient;
    private final ProductMapper productMapper;

    /**
     * Llama a: GET /product/{id}/similarids en el mock (puerto 3001)
     */
    public List<String> loadSimilarProductIds(String productId) {
        try {
            return simuladoRestClient.get()
                    .uri("/product/{id}/similarids", productId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                        throw new ProductNotFoundException(productId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new ExternalServiceException("Error calling similarids " + productId);
                    })
                    .body(new ParameterizedTypeReference<List<String>>() {
                    });
        } catch (RestClientException e) {
            // timeouts, I/O, etc. -> error externo
            throw new ExternalServiceException("Timeout/error calling similarids " + productId);
        }
    }


    /**
     * Llama a: GET /product/{id} en el mock y lo mapea a dominio.
     */
    public Product loadProductDetailForId(String productId) {
        try {
            SimulatedProductDto dto = simuladoRestClient.get()
                    .uri("/product/{id}", productId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new ProductNotFoundException(productId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new ExternalServiceException("Error calling simulado product " + productId);
                    })
                    .body(SimulatedProductDto.class);

            return productMapper.toDomain(dto);

        } catch (RestClientException e) { // aquí entran los timeouts
            throw new ExternalServiceException("Timeout/error calling simulado product " + productId);
        }
    }
}
