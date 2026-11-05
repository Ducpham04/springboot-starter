package com.example.starter.product.mapper;

import com.example.starter.product.dto.ProductRequest;
import com.example.starter.product.dto.ProductResponse;
import com.example.starter.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.BeanMapping;

@Mapper
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    @Mapping(target = "status", defaultValue = "ACTIVE")
    Product toEntity(ProductRequest request);

    /** PUT semantics, except a null status keeps the current value. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "status", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Product product, ProductRequest request);
}
