package com.example.starter.product;

import com.example.starter.category.service.CategoryService;
import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.inventory.entity.MovementType;
import com.example.starter.inventory.repository.StockMovementRepository;
import com.example.starter.inventory.service.StockMovementService;
import com.example.starter.product.dto.ProductRequest;
import com.example.starter.product.dto.ProductResponse;
import com.example.starter.product.entity.Product;
import com.example.starter.product.entity.ProductStatus;
import com.example.starter.product.mapper.ProductMapper;
import com.example.starter.product.mapper.ProductMapperImpl;
import com.example.starter.product.repository.ProductRepository;
import com.example.starter.product.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pure unit test: repositories mocked, real MapStruct mapper. */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private StockMovementService stockMovementService;
    @Mock
    private StockMovementRepository stockMovementRepository;

    private final ProductMapper mapper = new ProductMapperImpl();

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(repository, mapper, categoryService, stockMovementService, stockMovementRepository);
        loginWith("PRODUCT_READ", "PRODUCT_COST_VIEW");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static void loginWith(String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "u@test", null, AuthorityUtils.createAuthorityList(authorities)));
    }

    private static ProductRequest request(String sku, ProductStatus status, Integer quantity) {
        return new ProductRequest(sku, "Mì Hảo Hảo", "desc", null, "gói", new BigDecimal("4500"),
                new BigDecimal("3600"), quantity, 10, null, status);
    }

    @Test
    void createSavesProductWithDefaultsAndRecordsOpeningStock() {
        when(repository.existsBySkuIgnoreCase("P-1")).thenReturn(false);
        when(repository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductResponse response = service.create(request("P-1", null, 50));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(response.costPrice()).isEqualByComparingTo("3600");
        verify(stockMovementService).apply(any(Product.class), eq(MovementType.INITIAL), eq(50), any(), anyString());
    }

    @Test
    void createWithoutOpeningStockRecordsNoMovement() {
        when(repository.existsBySkuIgnoreCase("P-2")).thenReturn(false);
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(request("P-2", null, null));

        verify(stockMovementService, never()).apply(any(), any(), anyInt(), any(), any());
    }

    @Test
    void createRejectsDuplicateSku() {
        when(repository.existsBySkuIgnoreCase("P-1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("P-1", null, 0)))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_SKU_EXISTS);
        verify(repository, never()).save(any());
    }

    @Test
    void updateNeverChangesStockAndKeepsStatusWhenNotProvided() {
        Product existing = new Product();
        existing.setId(7L);
        existing.setSku("P-7");
        existing.setQuantity(42);
        existing.setStatus(ProductStatus.INACTIVE);
        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(repository.existsBySkuIgnoreCaseAndIdNot("P-7", 7L)).thenReturn(false);
        when(repository.saveAndFlush(existing)).thenReturn(existing);

        ProductResponse response = service.update(7L, request("P-7", null, 999));

        assertThat(response.name()).isEqualTo("Mì Hảo Hảo");
        assertThat(response.quantity()).isEqualTo(42);
        assertThat(response.status()).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    void costPriceIsHiddenWithoutPermission() {
        loginWith("PRODUCT_READ");
        Product p = new Product();
        p.setId(3L);
        p.setPrice(new BigDecimal("10000"));
        p.setCostPrice(new BigDecimal("8000"));
        when(repository.findWithCategoryById(3L)).thenReturn(Optional.of(p));

        assertThat(service.getById(3L).costPrice()).isNull();
    }

    @Test
    void deleteIsRejectedWhenProductHasTransactions() {
        Product p = new Product();
        p.setId(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(p));
        when(repository.hasTransactions(5L)).thenReturn(1L);

        assertThatThrownBy(() -> service.delete(5L))
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_IN_USE);
        verify(repository, never()).delete(any(Product.class));
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(repository.findWithCategoryById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }
}
