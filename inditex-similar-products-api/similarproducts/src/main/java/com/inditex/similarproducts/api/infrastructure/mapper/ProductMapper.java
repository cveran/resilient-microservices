package com.inditex.similarproducts.api.infrastructure.mapper;

import com.inditex.similarproducts.api.domain.Product;
import com.inditex.similarproducts.api.infrastructure.client.SimulatedProductDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toDomain(SimulatedProductDto dto);
}
