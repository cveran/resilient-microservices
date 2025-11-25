package com.inditex.similarproducts.api.infrastructure.client;

import com.inditex.similarproducts.api.domain.ExternalServiceException;
import com.inditex.similarproducts.api.domain.Product;
import com.inditex.similarproducts.api.domain.ProductNotFoundException;
import com.inditex.similarproducts.api.infrastructure.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
class SimulatedProductClientTest {

    private SimulatedProductClient client;

    private MockRestServiceServer mockServer;

    @Mock
    private ProductMapper productMapper;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:3001");

        mockServer = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
        client = new SimulatedProductClient(restClient, productMapper);
    }

    @Test
    void shouldMapProductDetailCorrectlyWhen200OK() {
        // GIVEN: el mock responde 200 con JSON del producto "1"
        String productId = "1";
        String jsonResponse = """
                {
                    "id": "1",
                    "name": "Shirt",
                    "price": 9.99,
                    "availability": true
                }
                """;

        mockServer.expect(requestTo("http://localhost:3001/product/1"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        Product expectedProduct = new Product("1", "Shirt", 9.99, true);
        when(productMapper.toDomain(any(SimulatedProductDto.class))).thenReturn(expectedProduct);

        // WHEN: llamamos al cliente
        Product result = client.loadProductDetailForId(productId);

        // THEN: el producto se mapea correctamente
        assertThat(result)
                .isNotNull()
                .extracting(Product::id, Product::name, Product::price, Product::availability)
                .containsExactly("1", "Shirt", 9.99, true);

        mockServer.verify();
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhen404() {
        // GIVEN: el mock responde 404 para producto "5"
        String productId = "5";
        String jsonResponse = """
                {
                    "message": "Product not found"
                }
                """;

        mockServer.expect(requestTo("http://localhost:3001/product/5"))
                  .andRespond(withStatus(HttpStatus.NOT_FOUND)
                                .body(jsonResponse)
                                .contentType(MediaType.APPLICATION_JSON));

        // WHEN & THEN: lanza ProductNotFoundException
        assertThatThrownBy(() -> client.loadProductDetailForId(productId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("5");

        mockServer.verify();
    }

    @Test
    void shouldThrowExternalServiceExceptionWhen500() {
        // GIVEN: el mock responde 500 para producto "6"
        String productId = "6";

        mockServer.expect(requestTo("http://localhost:3001/product/6"))
                .andRespond(withServerError());

        // WHEN & THEN: lanza ExternalServiceException
        assertThatThrownBy(() -> client.loadProductDetailForId(productId))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("6");

        mockServer.verify();
    }

    @Test
    void shouldLoadSimilarIdsCorrectlyWhen200_OK() {
        // GIVEN: el mock responde 200 con lista de IDs para producto "2"
        String productId = "2";
        String jsonResponse = """
                ["3", "100", "1000"]
                """;

        mockServer.expect(requestTo("http://localhost:3001/product/2/similarids"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // WHEN: llamamos al cliente para obtener IDs similares
        List<String> result = client.loadSimilarProductIds(productId);

        // THEN: devuelve la lista correcta
        assertThat(result)
                .isNotNull()
                .hasSize(3)
                .containsExactly("3", "100", "1000");

        mockServer.verify();
    }


    @Test
    void shouldThrowProductNotFoundExceptionWhenSimilarIds404() {
        // GIVEN: el mock responde 404 para similarids de producto "6"
        String productId = "6";

        mockServer.expect(requestTo("http://localhost:3001/product/6/similarids"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // WHEN & THEN: lanza ProductNotFoundException
        assertThatThrownBy(() -> client.loadSimilarProductIds(productId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("6");

        mockServer.verify();
    }
}

