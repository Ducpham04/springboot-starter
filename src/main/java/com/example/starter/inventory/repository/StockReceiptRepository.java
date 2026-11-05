package com.example.starter.inventory.repository;

import com.example.starter.inventory.entity.StockReceipt;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface StockReceiptRepository extends JpaRepository<StockReceipt, Long>,
        JpaSpecificationExecutor<StockReceipt> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<StockReceipt> findWithItemsById(Long id);
}
