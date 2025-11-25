package com.inditex.similarproducts.api.application;

import com.inditex.similarproducts.api.domain.ExternalServiceException;
import com.inditex.similarproducts.api.domain.Product;
import com.inditex.similarproducts.api.domain.ProductNotFoundException;
import com.inditex.similarproducts.api.infrastructure.client.SimulatedProductClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimilarProductsService {

    private final SimulatedProductClient simulatedProductClient;

    @Cacheable(cacheNames = "productDetail", key = "#productId")
    public List<Product> getSimilarProducts(String productId) {
        List<String> ids = simulatedProductClient.loadSimilarProductIds(productId)
                                                 .stream()
                                                 .distinct()
                                                 .toList();

        return ids.parallelStream()
                  .map(id -> {
                    try {return simulatedProductClient.loadProductDetailForId(id);}
                    catch (ProductNotFoundException | ExternalServiceException e)
                        {
                            log.warn("Skipping similar product {} for base {}: {}", id, productId, e.getMessage());
                            return null;
                        }
                    })
                .filter(Objects::nonNull)
                .toList();
    }

}
