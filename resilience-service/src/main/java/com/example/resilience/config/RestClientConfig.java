package com.example.resilience.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configuration for RestClient with timeout settings.
 * 
 * Demonstrates:
 * - Connection timeout configuration
 * - Read timeout configuration
 * - Custom request factory for fine-grained control
 */
@Configuration
public class RestClientConfig {

    @Value("${resilience.http.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${resilience.http.read-timeout:10000}")
    private int readTimeout;

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .requestFactory(clientHttpRequestFactory())
                .build();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        factory.setReadTimeout(Duration.ofMillis(readTimeout));
        return factory;
    }
}
