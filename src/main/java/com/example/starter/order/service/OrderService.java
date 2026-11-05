package com.example.starter.order.service;

import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.common.paging.SpecificationBuilder;
import com.example.starter.common.response.PageResponse;
import com.example.starter.customer.entity.Customer;
import com.example.starter.customer.repository.CustomerRepository;
import com.example.starter.inventory.entity.MovementType;
import com.example.starter.inventory.service.CodeGenerator;
import com.example.starter.inventory.service.StockMovementService;
import com.example.starter.order.dto.CancelOrderRequest;
import com.example.starter.order.dto.OrderFilter;
import com.example.starter.order.dto.OrderRequest;
import com.example.starter.order.dto.OrderResponse;
import com.example.starter.order.entity.OrderItem;
import com.example.starter.order.entity.OrderStatus;
import com.example.starter.order.entity.PaymentMethod;
import com.example.starter.order.entity.SalesOrder;
import com.example.starter.order.repository.OrderRepository;
import com.example.starter.product.entity.Product;
import com.example.starter.product.entity.ProductStatus;
import com.example.starter.product.repository.ProductRepository;
import com.example.starter.security.SecurityUtils;
import com.example.starter.security.UserPrincipal;
import com.example.starter.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.starter.common.util.DateRanges.endOfDay;
import static com.example.starter.common.util.DateRanges.startOfDay;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    public static final String READ_ALL = "ORDER_READ_ALL";
    public static final String COST_VIEW = "PRODUCT_COST_VIEW";

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final StockMovementService stockMovementService;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    /**
     * Creates a completed sale. Product rows are locked (FOR UPDATE, id order) so two cashiers can
     * never sell the same last unit; prices come from the product, not from the client.
     */
    @Transactional
    public OrderResponse create(OrderRequest request) {
        UserPrincipal current = SecurityUtils.requireUser();
        List<Long> ids = request.items().stream().map(OrderRequest.Item::productId).toList();
        if (ids.stream().distinct().count() != ids.size()) {
            throw new AppException(ErrorCode.DUPLICATE_LINE_ITEM);
        }
        Map<Long, Product> products = productRepository.findAllByIdForUpdate(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        SalesOrder order = new SalesOrder();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        for (OrderRequest.Item line : request.items()) {
            Product product = products.get(line.productId());
            if (product == null) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND, line.productId());
            }
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new AppException(ErrorCode.PRODUCT_INACTIVE, product.getName());
            }
            if (product.getQuantity() < line.quantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK, product.getName(), product.getQuantity());
            }
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setSku(product.getSku());
            item.setUnit(product.getUnit());
            item.setUnitPrice(product.getPrice());
            item.setUnitCost(product.getCostPrice());
            item.setQuantity(line.quantity());
            item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(line.quantity())));
            order.addItem(item);
            subtotal = subtotal.add(item.getLineTotal());
            totalCost = totalCost.add(product.getCostPrice().multiply(BigDecimal.valueOf(line.quantity())));
        }

        BigDecimal discount = request.discount() == null ? BigDecimal.ZERO : request.discount();
        if (discount.compareTo(subtotal) > 0) {
            throw new AppException(ErrorCode.DISCOUNT_TOO_LARGE);
        }
        BigDecimal total = subtotal.subtract(discount);
        BigDecimal cash = request.paymentMethod() == PaymentMethod.CASH ? request.cashReceived() : null;
        if (cash != null && cash.compareTo(total) < 0) {
            throw new AppException(ErrorCode.CASH_NOT_ENOUGH);
        }

        order.setCode(codeGenerator.next(CodeGenerator.ORDER));
        order.setSeller(userRepository.getReferenceById(current.getId()));
        order.setCustomer(request.customerId() == null ? null : findCustomer(request.customerId()));
        order.setPaymentMethod(request.paymentMethod());
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setTotal(total);
        order.setTotalCost(totalCost);
        order.setCashReceived(cash);
        order.setNote(blankToNull(request.note()));
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        for (OrderItem item : order.getItems()) {
            stockMovementService.apply(item.getProduct(), MovementType.SALE, -item.getQuantity(), order.getCode(), null);
        }
        return toResponse(order, true);
    }

    /** Cancels a completed order and returns its items to stock. */
    @Transactional
    public OrderResponse cancel(Long id, CancelOrderRequest request) {
        SalesOrder order = orderRepository.findDetailById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND, id));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }
        List<Long> productIds = order.getItems().stream().map(i -> i.getProduct().getId()).toList();
        Map<Long, Product> locked = productRepository.findAllByIdForUpdate(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        for (OrderItem item : order.getItems()) {
            stockMovementService.apply(locked.get(item.getProduct().getId()), MovementType.SALE_CANCEL,
                    item.getQuantity(), order.getCode(), request.reason().trim());
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(clock.instant());
        order.setCancelledBy(SecurityUtils.requireUsername());
        order.setCancelReason(request.reason().trim());
        orderRepository.saveAndFlush(order);
        return toResponse(order, true);
    }

    public PageResponse<OrderResponse> search(OrderFilter filter, Pageable pageable) {
        Long sellerId = canReadAll() ? filter.sellerId() : SecurityUtils.requireUser().getId();
        Specification<SalesOrder> spec = SpecificationBuilder.<SalesOrder>create()
                .greaterThanOrEqual("createdAt", filter.from() == null ? null : startOfDay(filter.from()))
                .lessThanOrEqual("createdAt", filter.to() == null ? null : endOfDay(filter.to()))
                .equal("status", filter.status())
                .equal("customer.id", filter.customerId())
                .equal("seller.id", sellerId)
                .likeAny(filter.keyword(), "code", "customer.name", "customer.phone")
                .build();
        return PageResponse.of(orderRepository.findAll(spec, pageable), o -> toResponse(o, false));
    }

    public OrderResponse getById(Long id) {
        SalesOrder order = orderRepository.findDetailById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND, id));
        if (!canReadAll() && !order.getSeller().getId().equals(SecurityUtils.requireUser().getId())) {
            // Do not reveal that the order exists
            throw new AppException(ErrorCode.ORDER_NOT_FOUND, id);
        }
        return toResponse(order, true);
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND, id));
    }

    private static boolean canReadAll() {
        return SecurityUtils.hasAuthority(READ_ALL);
    }

    OrderResponse toResponse(SalesOrder o, boolean withItems) {
        boolean showCost = SecurityUtils.hasAuthority(COST_VIEW);
        Customer c = o.getCustomer();
        OrderResponse.Party customer = c == null ? null : new OrderResponse.Party(c.getId(), c.getName(), c.getPhone());
        OrderResponse.Party seller = new OrderResponse.Party(o.getSeller().getId(), o.getSeller().getFullName(), null);
        BigDecimal change = o.getCashReceived() == null ? null : o.getCashReceived().subtract(o.getTotal());
        List<OrderResponse.Item> items = withItems
                ? o.getItems().stream().map(i -> new OrderResponse.Item(i.getProduct().getId(), i.getSku(),
                        i.getProductName(), i.getUnit(), i.getUnitPrice(), showCost ? i.getUnitCost() : null,
                        i.getQuantity(), i.getLineTotal())).toList()
                : null;
        return new OrderResponse(o.getId(), o.getCode(), o.getStatus(), o.getPaymentMethod(), customer, seller,
                o.getSubtotal(), o.getDiscount(), o.getTotal(),
                showCost ? o.getTotalCost() : null,
                showCost ? o.getTotal().subtract(o.getTotalCost()) : null,
                o.getCashReceived(), change, o.getNote(), o.getCreatedAt(),
                o.getCancelledAt(), o.getCancelledBy(), o.getCancelReason(), items);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
