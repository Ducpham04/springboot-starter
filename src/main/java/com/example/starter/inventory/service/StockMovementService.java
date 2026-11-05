package com.example.starter.inventory.service;

import com.example.starter.inventory.entity.MovementType;
import com.example.starter.inventory.entity.StockMovement;
import com.example.starter.inventory.repository.StockMovementRepository;
import com.example.starter.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Single entry point for changing stock: applies the delta to the (already locked) product and
 * writes the ledger line in the same transaction.
 */
@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository repository;

    @Transactional(propagation = Propagation.MANDATORY)
    public StockMovement apply(Product product, MovementType type, int change, String referenceCode, String note) {
        int balance = product.getQuantity() + change;
        if (balance < 0) {
            throw new IllegalStateException("Negative stock for product " + product.getId());
        }
        product.setQuantity(balance);
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setType(type);
        movement.setQuantityChange(change);
        movement.setBalanceAfter(balance);
        movement.setReferenceCode(referenceCode);
        movement.setNote(note);
        return repository.save(movement);
    }
}
