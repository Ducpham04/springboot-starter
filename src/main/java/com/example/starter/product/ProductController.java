package com.example.starter.product;

import com.example.starter.common.response.ApiResponse;
import com.example.starter.common.response.PageResponse;
import com.example.starter.product.dto.ProductFilter;
import com.example.starter.product.dto.ProductRequest;
import com.example.starter.product.dto.ProductResponse;
import com.example.starter.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reference CRUD module. Copy the whole {@code product} package to start a new module:
 * entity → repository → dto → mapper → service → controller, plus a Flyway migration
 * and permissions (see V3__product.sql).
 */
@Tag(name = "Products")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Search products with paging, sorting and dynamic filters")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> search(
            @ParameterObject ProductFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ApiResponse.ok(productService.search(filter, pageable));
    }

    @Operation(summary = "Get a product by id")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(productService.getById(id));
    }

    @Operation(summary = "Create a product")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ApiResponse.ok(productService.create(request));
    }

    @Operation(summary = "Update a product")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ApiResponse.ok(productService.update(id, request));
    }

    @Operation(summary = "Delete a product (ADMIN role only - example of hasRole)")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PRODUCT_DELETE')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.ok(null);
    }
}
