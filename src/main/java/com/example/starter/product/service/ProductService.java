package com.example.starter.product.service;

import com.example.starter.category.service.CategoryService;
import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.common.paging.SpecificationBuilder;
import com.example.starter.common.response.PageResponse;
import com.example.starter.inventory.entity.MovementType;
import com.example.starter.inventory.repository.StockMovementRepository;
import com.example.starter.inventory.service.StockMovementService;
import com.example.starter.product.dto.ProductFilter;
import com.example.starter.product.dto.ProductRequest;
import com.example.starter.product.dto.ProductResponse;
import com.example.starter.product.entity.Product;
import com.example.starter.product.mapper.ProductMapper;
import com.example.starter.product.repository.ProductRepository;
import com.example.starter.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    public static final String COST_VIEW = "PRODUCT_COST_VIEW";

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final StockMovementService stockMovementService;
    private final StockMovementRepository stockMovementRepository;

    public PageResponse<ProductResponse> search(ProductFilter filter, Pageable pageable) {
        Specification<Product> lowStock = Boolean.TRUE.equals(filter.lowStock())
                ? (root, q, cb) -> cb.le(root.get("quantity"), root.get("minStock"))
                : null;
        Specification<Product> spec = SpecificationBuilder.<Product>create()
                .likeAny(filter.keyword(), "name", "sku")
                .equal("status", filter.status())
                .equal("category.id", filter.categoryId())
                .between("price", filter.minPrice(), filter.maxPrice())
                .and(lowStock)
                .build();
        return PageResponse.of(productRepository.findAll(spec, pageable), this::toResponse);
    }

    public ProductResponse getById(Long id) {
        return toResponse(productRepository.findWithCategoryById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND, id)));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new AppException(ErrorCode.PRODUCT_SKU_EXISTS, request.sku());
        }
        Product product = productMapper.toEntity(request);
        product.setCategory(request.categoryId() == null ? null : categoryService.getEntity(request.categoryId()));
        int opening = product.getQuantity();
        product.setQuantity(0);
        productRepository.save(product);
        if (opening > 0) {
            stockMovementService.apply(product, MovementType.INITIAL, opening, null, "Tồn đầu kỳ");
        }
        return toResponse(product);
    }

    /** Updates descriptive fields and prices. Stock is never changed here (see inventory). */
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findOrThrow(id);
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(request.sku(), id)) {
            throw new AppException(ErrorCode.PRODUCT_SKU_EXISTS, request.sku());
        }
        productMapper.update(product, request);
        product.setCategory(request.categoryId() == null ? null : categoryService.getEntity(request.categoryId()));
        return toResponse(productRepository.saveAndFlush(product));
    }

    /** Products with sales or receipts cannot be deleted; deactivate them instead. */
    @Transactional
    public void delete(Long id) {
        Product product = findOrThrow(id);
        if (productRepository.hasTransactions(id) > 0) {
            throw new AppException(ErrorCode.PRODUCT_IN_USE);
        }
        stockMovementRepository.deleteByProductId(id);
        productRepository.delete(product);
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND, id));
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = productMapper.toResponse(product);
        return SecurityUtils.hasAuthority(COST_VIEW) ? response : response.withoutCost();
    }
}
