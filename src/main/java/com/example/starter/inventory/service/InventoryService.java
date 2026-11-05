package com.example.starter.inventory.service;

import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.common.paging.SpecificationBuilder;
import com.example.starter.common.response.PageResponse;
import com.example.starter.inventory.dto.StockAdjustmentRequest;
import com.example.starter.inventory.dto.StockMovementFilter;
import com.example.starter.inventory.dto.StockMovementResponse;
import com.example.starter.inventory.dto.StockReceiptFilter;
import com.example.starter.inventory.dto.StockReceiptRequest;
import com.example.starter.inventory.dto.StockReceiptResponse;
import com.example.starter.inventory.entity.MovementType;
import com.example.starter.inventory.entity.StockMovement;
import com.example.starter.inventory.entity.StockReceipt;
import com.example.starter.inventory.entity.StockReceiptItem;
import com.example.starter.inventory.repository.StockMovementRepository;
import com.example.starter.inventory.repository.StockReceiptRepository;
import com.example.starter.product.entity.Product;
import com.example.starter.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.starter.common.util.DateRanges.endOfDay;
import static com.example.starter.common.util.DateRanges.startOfDay;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final StockReceiptRepository receiptRepository;
    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final StockMovementService movementService;
    private final CodeGenerator codeGenerator;

    /**
     * Receives goods: increases stock and recomputes the moving-average cost
     * {@code (oldQty*oldCost + qty*unitCost) / (oldQty + qty)}.
     */
    @Transactional
    public StockReceiptResponse createReceipt(StockReceiptRequest request) {
        Map<Long, Product> products = lockProducts(request.items().stream()
                .map(StockReceiptRequest.Item::productId).toList());

        StockReceipt receipt = new StockReceipt();
        receipt.setCode(codeGenerator.next(CodeGenerator.RECEIPT));
        receipt.setSupplierName(trimToNull(request.supplierName()));
        receipt.setNote(trimToNull(request.note()));

        BigDecimal total = BigDecimal.ZERO;
        for (StockReceiptRequest.Item line : request.items()) {
            Product product = products.get(line.productId());
            BigDecimal lineTotal = line.unitCost().multiply(BigDecimal.valueOf(line.quantity()));
            product.setCostPrice(movingAverage(product.getQuantity(), product.getCostPrice(),
                    line.quantity(), line.unitCost()));
            movementService.apply(product, MovementType.RECEIPT, line.quantity(), receipt.getCode(),
                    receipt.getSupplierName());

            StockReceiptItem item = new StockReceiptItem();
            item.setProduct(product);
            item.setQuantity(line.quantity());
            item.setUnitCost(line.unitCost());
            item.setLineTotal(lineTotal);
            receipt.addItem(item);
            total = total.add(lineTotal);
        }
        receipt.setTotalAmount(total);
        return toResponse(receiptRepository.save(receipt), true);
    }

    @Transactional
    public StockMovementResponse adjust(StockAdjustmentRequest request) {
        Product product = lockProducts(List.of(request.productId())).get(request.productId());
        int diff = request.actualQuantity() - product.getQuantity();
        if (diff == 0) {
            throw new AppException(ErrorCode.STOCK_ADJUSTMENT_NO_CHANGE);
        }
        return toResponse(movementService.apply(product, MovementType.ADJUSTMENT, diff, null, request.reason().trim()));
    }

    public PageResponse<StockReceiptResponse> searchReceipts(StockReceiptFilter filter, Pageable pageable) {
        var spec = SpecificationBuilder.<StockReceipt>create()
                .likeAny(filter.keyword(), "code", "supplierName", "note")
                .greaterThanOrEqual("createdAt", filter.from() == null ? null : startOfDay(filter.from()))
                .lessThanOrEqual("createdAt", filter.to() == null ? null : endOfDay(filter.to()))
                .build();
        return PageResponse.of(receiptRepository.findAll(spec, pageable), r -> toResponse(r, false));
    }

    public StockReceiptResponse getReceipt(Long id) {
        return receiptRepository.findWithItemsById(id)
                .map(r -> toResponse(r, true))
                .orElseThrow(() -> new AppException(ErrorCode.RECEIPT_NOT_FOUND, id));
    }

    public PageResponse<StockMovementResponse> searchMovements(StockMovementFilter filter, Pageable pageable) {
        var spec = SpecificationBuilder.<StockMovement>create()
                .equal("product.id", filter.productId())
                .equal("type", filter.type())
                .likeAny(filter.keyword(), "product.name", "product.sku", "referenceCode")
                .build();
        return PageResponse.of(movementRepository.findAll(spec, pageable), this::toResponse);
    }

    /** Locks the products (FOR UPDATE, id order) and fails if any id is unknown. */
    Map<Long, Product> lockProducts(List<Long> ids) {
        List<Long> distinct = ids.stream().distinct().toList();
        if (distinct.size() != ids.size()) {
            throw new AppException(ErrorCode.DUPLICATE_LINE_ITEM);
        }
        Map<Long, Product> products = productRepository.findAllByIdForUpdate(distinct).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        for (Long id : distinct) {
            if (!products.containsKey(id)) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND, id);
            }
        }
        return products;
    }

    static BigDecimal movingAverage(int oldQty, BigDecimal oldCost, int addQty, BigDecimal addCost) {
        if (oldQty <= 0 || oldCost == null) {
            return addCost.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal value = oldCost.multiply(BigDecimal.valueOf(oldQty))
                .add(addCost.multiply(BigDecimal.valueOf(addQty)));
        return value.divide(BigDecimal.valueOf((long) oldQty + addQty), 2, RoundingMode.HALF_UP);
    }

    private StockReceiptResponse toResponse(StockReceipt r, boolean withItems) {
        List<StockReceiptResponse.Item> items = withItems
                ? r.getItems().stream().map(i -> new StockReceiptResponse.Item(
                        i.getProduct().getId(), i.getProduct().getSku(), i.getProduct().getName(),
                        i.getProduct().getUnit(), i.getQuantity(), i.getUnitCost(), i.getLineTotal())).toList()
                : null;
        int count = withItems ? r.getItems().size() : receiptItemCount(r);
        return new StockReceiptResponse(r.getId(), r.getCode(), r.getSupplierName(), r.getNote(),
                r.getTotalAmount(), count, r.getCreatedAt(), r.getCreatedBy(), items);
    }

    private int receiptItemCount(StockReceipt r) {
        // Lazy collection size: Hibernate issues a cheap count only when the batch is not initialized.
        return org.hibernate.Hibernate.size(r.getItems());
    }

    private StockMovementResponse toResponse(StockMovement m) {
        Product p = m.getProduct();
        return new StockMovementResponse(m.getId(), p.getId(), p.getSku(), p.getName(), m.getType(),
                m.getQuantityChange(), m.getBalanceAfter(), m.getReferenceCode(), m.getNote(),
                m.getCreatedAt(), m.getCreatedBy());
    }

    private static String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
