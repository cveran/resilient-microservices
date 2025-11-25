package com.inditex.similarproducts.api.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI inditexOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                    .title("Inditex Similar Product API")
                    .version("1.0.0")
                    .description("Similar products and their details based on the product ID entered")
                    .contact(new Contact()
                            .name("Inditex Digital Team")
                            .url("https://www.inditex.com/itxcomweb/es/es/home")
                            .email("christian.vera.nag@gmail.com"))

                    .license(new License()
                            .name("Apache 2.0")));
    }
}
