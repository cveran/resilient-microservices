package com.inditex.similarproducts.api.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "simulado.timeouts")
public class SimuladoTimeoutProperties {
    private int connectMs;
    private int responseMs;
}
