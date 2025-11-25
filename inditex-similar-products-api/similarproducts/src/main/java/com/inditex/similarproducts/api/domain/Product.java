package com.inditex.similarproducts.api.domain;


public record Product(
        String id,
        String name,
        Double price,
        Boolean availability
) {}
