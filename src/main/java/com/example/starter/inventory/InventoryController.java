package com.example.starter.inventory;

import com.example.starter.common.response.ApiResponse;
import com.example.starter.common.response.PageResponse;
import com.example.starter.inventory.dto.StockAdjustmentRequest;
import com.example.starter.inventory.dto.StockMovementFilter;
import com.example.starter.inventory.dto.StockMovementResponse;
import com.example.starter.inventory.dto.StockReceiptFilter;
import com.example.starter.inventory.dto.StockReceiptRequest;
import com.example.starter.inventory.dto.StockReceiptResponse;
import com.example.starter.inventory.service.InventoryService;
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

@Tag(name = "Inventory")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Search stock receipts")
    @PreAuthorize("hasAuthority('STOCK_READ')")
    @GetMapping("/stock-receipts")
    public ApiResponse<PageResponse<StockReceiptResponse>> receipts(
            @ParameterObject StockReceiptFilter filter,
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(inventoryService.searchReceipts(filter, pageable));
    }

    @PreAuthorize("hasAuthority('STOCK_READ')")
    @GetMapping("/stock-receipts/{id}")
    public ApiResponse<StockReceiptResponse> receipt(@PathVariable Long id) {
        return ApiResponse.ok(inventoryService.getReceipt(id));
    }

    @Operation(summary = "Receive goods: increases stock and updates moving-average cost")
    @PreAuthorize("hasAuthority('STOCK_MANAGE')")
    @PostMapping("/stock-receipts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StockReceiptResponse> createReceipt(@Valid @RequestBody StockReceiptRequest request) {
        return ApiResponse.ok(inventoryService.createReceipt(request));
    }

    @Operation(summary = "Stock movement ledger")
    @PreAuthorize("hasAuthority('STOCK_READ')")
    @GetMapping("/stock-movements")
    public ApiResponse<PageResponse<StockMovementResponse>> movements(
            @ParameterObject StockMovementFilter filter,
            @ParameterObject @PageableDefault(size = 30, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(inventoryService.searchMovements(filter, pageable));
    }

    @Operation(summary = "Stock count: set the actual quantity")
    @PreAuthorize("hasAuthority('STOCK_MANAGE')")
    @PostMapping("/stock-adjustments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StockMovementResponse> adjust(@Valid @RequestBody StockAdjustmentRequest request) {
        return ApiResponse.ok(inventoryService.adjust(request));
    }
}
