package com.inditex.similarproducts.api.infrastructure.client;

import lombok.Data;

@Data
public class SimulatedProductDto {
    private String id;
    private String name;
    private Double price;
    private Boolean availability;
}
