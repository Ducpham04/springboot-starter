package com.example.starter.order;

import com.example.starter.common.response.ApiResponse;
import com.example.starter.common.response.PageResponse;
import com.example.starter.order.dto.CancelOrderRequest;
import com.example.starter.order.dto.OrderFilter;
import com.example.starter.order.dto.OrderRequest;
import com.example.starter.order.dto.OrderResponse;
import com.example.starter.order.service.OrderService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Orders")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Search orders (without ORDER_READ_ALL only the caller's own orders)")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> search(
            @ParameterObject OrderFilter filter,
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(orderService.search(filter, pageable));
    }

    @PreAuthorize("hasAuthority('ORDER_READ')")
    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getById(id));
    }

    @Operation(summary = "Create a sale (POS). Decreases stock.")
    @PreAuthorize("hasAuthority('ORDER_CREATE')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.ok(orderService.create(request));
    }

    @Operation(summary = "Cancel an order and return its items to stock")
    @PreAuthorize("hasAuthority('ORDER_CANCEL')")
    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(@PathVariable Long id, @Valid @RequestBody CancelOrderRequest request) {
        return ApiResponse.ok(orderService.cancel(id, request));
    }
}
