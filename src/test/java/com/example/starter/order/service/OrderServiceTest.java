package com.example.starter.order.service;

import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.customer.repository.CustomerRepository;
import com.example.starter.inventory.entity.MovementType;
import com.example.starter.inventory.service.CodeGenerator;
import com.example.starter.inventory.service.StockMovementService;
import com.example.starter.order.dto.OrderRequest;
import com.example.starter.order.dto.OrderResponse;
import com.example.starter.order.entity.PaymentMethod;
import com.example.starter.order.entity.SalesOrder;
import com.example.starter.order.repository.OrderRepository;
import com.example.starter.product.entity.Product;
import com.example.starter.product.entity.ProductStatus;
import com.example.starter.product.repository.ProductRepository;
import com.example.starter.security.UserPrincipal;
import com.example.starter.user.entity.User;
import com.example.starter.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StockMovementService stockMovementService;
    @Mock
    private CodeGenerator codeGenerator;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(orderRepository, productRepository, customerRepository, userRepository,
                stockMovementService, codeGenerator, Clock.systemUTC());
        UserPrincipal principal = new UserPrincipal(7L, "staff@test", "x", true, 0,
                Set.of(new SimpleGrantedAuthority("ORDER_CREATE")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        User seller = new User();
        seller.setId(7L);
        seller.setFullName("Hoa");
        lenient().when(userRepository.getReferenceById(7L)).thenReturn(seller);
        lenient().when(codeGenerator.next(CodeGenerator.ORDER)).thenReturn("DH260105-0001");
        lenient().when(orderRepository.save(any(SalesOrder.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static Product product(long id, String price, String cost, int stock, ProductStatus status) {
        Product p = new Product();
        p.setId(id);
        p.setSku("SKU" + id);
        p.setName("Sản phẩm " + id);
        p.setPrice(new BigDecimal(price));
        p.setCostPrice(new BigDecimal(cost));
        p.setQuantity(stock);
        p.setStatus(status);
        return p;
    }

    private static OrderRequest request(BigDecimal discount, PaymentMethod method, BigDecimal cash, OrderRequest.Item... items) {
        return new OrderRequest(null, List.of(items), discount, method, cash, null);
    }

    @Test
    void usesServerPricesAndRecordsSaleMovements() {
        when(productRepository.findAllByIdForUpdate(anyList())).thenReturn(List.of(
                product(1, "15000", "10000", 10, ProductStatus.ACTIVE),
                product(2, "4500", "3600", 5, ProductStatus.ACTIVE)));

        OrderResponse response = service.create(request(new BigDecimal("1000"), PaymentMethod.CASH,
                new BigDecimal("50000"), new OrderRequest.Item(1L, 2), new OrderRequest.Item(2L, 1)));

        assertThat(response.code()).isEqualTo("DH260105-0001");
        assertThat(response.subtotal()).isEqualByComparingTo("34500");
        assertThat(response.total()).isEqualByComparingTo("33500");
        assertThat(response.change()).isEqualByComparingTo("16500");
        assertThat(response.profit()).as("no PRODUCT_COST_VIEW").isNull();
        verify(stockMovementService).apply(any(Product.class), eq(MovementType.SALE), eq(-2), eq("DH260105-0001"), any());
        verify(stockMovementService).apply(any(Product.class), eq(MovementType.SALE), eq(-1), eq("DH260105-0001"), any());
    }

    @Test
    void rejectsInsufficientStock() {
        when(productRepository.findAllByIdForUpdate(anyList()))
                .thenReturn(List.of(product(1, "15000", "10000", 1, ProductStatus.ACTIVE)));

        assertThatThrownBy(() -> service.create(request(null, PaymentMethod.TRANSFER, null, new OrderRequest.Item(1L, 2))))
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
        verify(orderRepository, never()).save(any());
        verify(stockMovementService, never()).apply(any(), any(), anyInt(), any(), any());
    }

    @Test
    void rejectsInactiveProductAndUnknownProduct() {
        when(productRepository.findAllByIdForUpdate(anyList()))
                .thenReturn(List.of(product(1, "15000", "10000", 10, ProductStatus.INACTIVE)));
        assertThatThrownBy(() -> service.create(request(null, PaymentMethod.CARD, null, new OrderRequest.Item(1L, 1))))
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_INACTIVE);

        when(productRepository.findAllByIdForUpdate(anyList())).thenReturn(List.of());
        assertThatThrownBy(() -> service.create(request(null, PaymentMethod.CARD, null, new OrderRequest.Item(9L, 1))))
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void rejectsDuplicateLinesDiscountAndCash() {
        assertThatThrownBy(() -> service.create(request(null, PaymentMethod.CASH, null,
                new OrderRequest.Item(1L, 1), new OrderRequest.Item(1L, 2))))
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_LINE_ITEM);

        when(productRepository.findAllByIdForUpdate(anyList()))
                .thenReturn(List.of(product(1, "10000", "8000", 10, ProductStatus.ACTIVE)));
        assertThatThrownBy(() -> service.create(request(new BigDecimal("10001"), PaymentMethod.CASH, null,
                new OrderRequest.Item(1L, 1))))
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.DISCOUNT_TOO_LARGE);
        assertThatThrownBy(() -> service.create(request(null, PaymentMethod.CASH, new BigDecimal("9999"),
                new OrderRequest.Item(1L, 1))))
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.CASH_NOT_ENOUGH);
    }

    @Test
    void cashReceivedIsIgnoredForNonCashPayments() {
        when(productRepository.findAllByIdForUpdate(anyList()))
                .thenReturn(List.of(product(1, "10000", "8000", 10, ProductStatus.ACTIVE)));

        OrderResponse response = service.create(request(null, PaymentMethod.TRANSFER, new BigDecimal("1"),
                new OrderRequest.Item(1L, 1)));

        assertThat(response.cashReceived()).isNull();
        assertThat(response.change()).isNull();
    }
}
