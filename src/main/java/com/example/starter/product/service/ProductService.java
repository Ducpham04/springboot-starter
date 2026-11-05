package com.example.starter.product.service;

import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.common.paging.SpecificationBuilder;
import com.example.starter.common.response.PageResponse;
import com.example.starter.product.dto.ProductFilter;
import com.example.starter.product.dto.ProductRequest;
import com.example.starter.product.dto.ProductResponse;
import com.example.starter.product.entity.Product;
import com.example.starter.product.mapper.ProductMapper;
import com.example.starter.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public PageResponse<ProductResponse> search(ProductFilter filter, Pageable pageable) {
        Specification<Product> spec = SpecificationBuilder.<Product>create()
                .likeAny(filter.keyword(), "name", "sku", "description")
                .equal("status", filter.status())
                .in("category", filter.category())
                .between("price", filter.minPrice(), filter.maxPrice())
                .build();
        return PageResponse.of(productRepository.findAll(spec, pageable), productMapper::toResponse);
    }

    public ProductResponse getById(Long id) {
        return productMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new AppException(ErrorCode.PRODUCT_SKU_EXISTS, request.sku());
        }
        Product saved = productRepository.save(productMapper.toEntity(request));
        return productMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findOrThrow(id);
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(request.sku(), id)) {
            throw new AppException(ErrorCode.PRODUCT_SKU_EXISTS, request.sku());
        }
        productMapper.update(product, request);
        return productMapper.toResponse(productRepository.saveAndFlush(product));
    }

    @Transactional
    public void delete(Long id) {
        productRepository.delete(findOrThrow(id));
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND, id));
    }
}
