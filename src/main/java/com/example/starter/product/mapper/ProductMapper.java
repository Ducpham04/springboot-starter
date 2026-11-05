package com.example.starter.product.mapper;

import com.example.starter.product.dto.ProductRequest;
import com.example.starter.product.dto.ProductResponse;
import com.example.starter.product.entity.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "lowStock", expression = "java(product.isLowStock())")
    ProductResponse toResponse(Product product);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "status", defaultValue = "ACTIVE")
    @Mapping(target = "unit", defaultValue = "cái")
    @Mapping(target = "costPrice", defaultValue = "0")
    @Mapping(target = "quantity", defaultValue = "0")
    @Mapping(target = "minStock", defaultValue = "0")
    Product toEntity(ProductRequest request);

    /**
     * PUT semantics for descriptive fields. Stock (quantity) is never touched here; null status,
     * unit, cost price and min stock keep their current value.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "quantity", ignore = true)
    @Mapping(target = "status", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "unit", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "costPrice", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "minStock", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Product product, ProductRequest request);
}
