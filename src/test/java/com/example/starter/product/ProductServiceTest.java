package com.example.starter.product;

import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.product.dto.ProductRequest;
import com.example.starter.product.dto.ProductResponse;
import com.example.starter.product.entity.Product;
import com.example.starter.product.entity.ProductStatus;
import com.example.starter.product.mapper.ProductMapper;
import com.example.starter.product.mapper.ProductMapperImpl;
import com.example.starter.product.repository.ProductRepository;
import com.example.starter.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pure unit test: repository mocked, real MapStruct mapper. */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    private final ProductMapper mapper = new ProductMapperImpl();

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(repository, mapper);
    }

    private static ProductRequest request(String sku, ProductStatus status) {
        return new ProductRequest(sku, "Phone", "desc", new BigDecimal("10.50"), 5, "mobile", null, status);
    }

    @Test
    void createSavesProductWithDefaultStatus() {
        when(repository.existsBySkuIgnoreCase("P-1")).thenReturn(false);
        when(repository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductResponse response = service.create(request("P-1", null));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(response.price()).isEqualByComparingTo("10.50");
    }

    @Test
    void createRejectsDuplicateSku() {
        when(repository.existsBySkuIgnoreCase("P-1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("P-1", null)))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_SKU_EXISTS);
        verify(repository, never()).save(any());
    }

    @Test
    void updateKeepsStatusWhenNotProvided() {
        Product existing = new Product();
        existing.setId(7L);
        existing.setSku("P-7");
        existing.setStatus(ProductStatus.INACTIVE);
        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(repository.existsBySkuIgnoreCaseAndIdNot("P-7", 7L)).thenReturn(false);
        when(repository.saveAndFlush(existing)).thenReturn(existing);

        ProductResponse response = service.update(7L, request("P-7", null));

        assertThat(response.name()).isEqualTo("Phone");
        assertThat(response.status()).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }
}
