package com.inditex.similarproducts.api.infrastructure.web;

import com.inditex.similarproducts.api.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integración del endpoint /product/{productId}/similar
 *
 * PREREQUISITO: El mock externo debe estar corriendo en http://localhost:3001
 *
 * Para ejecutar estos tests:
 * - Desde IntelliJ: Click derecho en la clase → Run 'SimilarProductsControllerTest'
 * - Desde terminal: .\mvnw.cmd test -Dtest=SimilarProductsControllerTest
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimilarProductsControllerTest {

    @LocalServerPort
    private int port;

    private RestClient restClient()
    {
        return RestClient.builder()
                         .baseUrl("http://localhost:" + port)
                         .build();
    }

    @Test
    void shouldReturnSimilarProductsForProduct1() {
        // GIVEN: productId "1" tiene similares ["2","3","4"] en el mock real
        // WHEN: GET /product/1/similar
        List<Product> products = restClient().get()
                                             .uri("/product/1/similar")
                                             .retrieve()
                                             .body(new ParameterizedTypeReference<List<Product>>() {});

        // THEN: lista con 3 productos
        assertThat(products).isNotNull().hasSize(3);

        List<String> ids = products.stream().map(Product::id).toList();
        assertThat(ids).containsExactlyInAnyOrder("2", "3", "4");

        // Validar detalles de cada producto según el mock real
        Product product2 = products.stream().filter(p -> "2".equals(p.id())).findFirst().orElseThrow();
        assertThat(product2.name()).isEqualTo("Dress");
        assertThat(product2.price()).isEqualTo(19.99);
        assertThat(product2.availability()).isTrue();

        Product product3 = products.stream().filter(p -> "3".equals(p.id())).findFirst().orElseThrow();
        assertThat(product3.name()).isEqualTo("Blazer");
        assertThat(product3.price()).isEqualTo(29.99);
        assertThat(product3.availability()).isFalse();

        Product product4 = products.stream().filter(p -> "4".equals(p.id())).findFirst().orElseThrow();
        assertThat(product4.name()).isEqualTo("Boots");
        assertThat(product4.price()).isEqualTo(39.99);
        assertThat(product4.availability()).isTrue();
    }

    @Test
    void shouldReturnSimilarProductsForProduct2() {
        // GIVEN: productId "2" tiene similares ["3","100","1000"] en el mock real
        // WHEN: GET /product/2/similar
        List<Product> products = restClient().get()
                                             .uri("/product/2/similar")
                                             .retrieve()
                                             .body(new ParameterizedTypeReference<List<Product>>() {});

        // THEN: lista con 3 productos
        assertThat(products).isNotNull().hasSize(3);

        List<String> ids = products.stream().map(Product::id).toList();
        assertThat(ids).containsExactlyInAnyOrder("3", "100", "1000");
    }

    @Test
    void shouldFilterOutTimeoutProductForProduct3() {
        // GIVEN: productId "3" tiene similares ["100","1000","10000"]
        //        pero "10000" tarda 50s / timeout
        // WHEN: GET /product/3/similar
        List<Product> products = restClient().get()
                                             .uri("/product/3/similar")
                                             .retrieve()
                                             .body(new ParameterizedTypeReference<List<Product>>() {});

        // THEN: lista con SOLO 2 productos (sin "10000")
        assertThat(products).isNotNull().hasSize(2);

        List<String> ids = products.stream().map(Product::id).toList();
        assertThat(ids)
                .containsExactlyInAnyOrder("100", "1000")
                .doesNotContain("10000");
    }

    @Test
    void shouldFilterOutNotFoundProductForProduct4() {
        // GIVEN: productId "4" tiene similares ["1","2","5"]
        //        pero "5" no existe (404 en el mock)
        // WHEN: GET /product/4/similar
        List<Product> products = restClient().get()
                                             .uri("/product/4/similar")
                                             .retrieve()
                                             .body(new ParameterizedTypeReference<List<Product>>() {});

        // THEN: lista con SOLO 2 productos (sin "5")
        assertThat(products).isNotNull().hasSize(2);

        List<String> ids = products.stream().map(Product::id).toList();
        assertThat(ids).containsExactlyInAnyOrder("1", "2").doesNotContain("5");
    }

    @Test
    void shouldFilterOut500ProductForProduct5() {
        // GIVEN: productId "5" tiene similares ["1","2","6"]
        //        pero "6" da 500 error en el mock
        // WHEN: GET /product/5/similar
        List<Product> products = restClient()
                .get()
                .uri("/product/5/similar")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Product>>() {});

        // THEN: lista con SOLO 2 productos (sin "6")
        assertThat(products).isNotNull().hasSize(2);

        List<String> ids = products.stream().map(Product::id).toList();
        assertThat(ids).containsExactlyInAnyOrder("1", "2").doesNotContain("6");
    }

    @Test
    void shouldReturn404WhenProductNotFound() {
        // GIVEN: productId "6" no existe (el mock devuelve 404 en /product/6/similarids)
        // WHEN: GET /product/6/similar
        // THEN: lanza HttpClientErrorException con status 404 y mensaje claro para el usuario
        assertThatThrownBy(() ->
                restClient()
                        .get()
                        .uri("/product/6/similar")
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<Product>>() {})
        )
        .isInstanceOf(HttpClientErrorException.class)
        .satisfies(ex -> {
            HttpClientErrorException httpEx = (HttpClientErrorException) ex;
            assertThat(httpEx.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

            // Verificar que el body contiene el mensaje de error para el usuario
            String responseBody = httpEx.getResponseBodyAsString();
            assertThat(responseBody)
                    .contains("Product not found")
                    .contains("Not Found");
        });
    }

    @Test
    void shouldReturn400WhenProductIdIsBlank() {
        // GIVEN: productId vacío (validación @NotBlank)
        // WHEN: GET /product/ /similar (con espacio)
        // THEN: lanza HttpClientErrorException con status 400 y mensaje claro para el usuario
        assertThatThrownBy(() -> restClient().get()
                                            .uri("/product/ /similar")
                                            .retrieve()
                                            .body(new ParameterizedTypeReference<List<Product>>() {})
        )
        .isInstanceOf(HttpClientErrorException.class)
        .satisfies(ex -> {
            HttpClientErrorException httpEx = (HttpClientErrorException) ex;
            assertThat(httpEx.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Verificar que el body contiene el mensaje de error para el usuario
            String responseBody = httpEx.getResponseBodyAsString();
            assertThat(responseBody)
                    .contains("Product ID is required")
                    .contains("Bad Request");
        });
    }

    @Test
    void shouldReturn400WhenProductIdIsEmpty() {
        // GIVEN: productId completamente vacío (doble barra en URL)
        // WHEN: GET /product//similar
        // THEN: debe devolver 400 Bad Request (nuestro handler convierte NoResourceFoundException a 400)
        assertThatThrownBy(() -> restClient().get()
                                            .uri("/product//similar")
                                            .retrieve()
                                            .body(new ParameterizedTypeReference<List<Product>>() {})
        )
        .isInstanceOf(HttpClientErrorException.class)
        .satisfies(ex -> {
            HttpClientErrorException httpEx = (HttpClientErrorException) ex;
            // Debe ser 400 Bad Request, no 500
            assertThat(httpEx.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Verificar que el body contiene el mensaje de error claro para el usuario
            String responseBody = httpEx.getResponseBodyAsString();
            assertThat(responseBody)
                    .contains("Product ID is required")
                    .contains("Bad Request");
        });
    }

    /**
     * NOTA sobre validación de productId:
     * - /product/ /similar (con espacio) → @NotBlank valida → 400
     * - /product//similar (doble barra) → Spring NoResourceFoundException → handler lo convierte a 400
     * Ambos casos ahora devuelven 400 Bad Request con mensaje "Product ID is required"
     */
}

