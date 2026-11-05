package com.example.starter.inventory.repository;

import com.example.starter.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long>,
        JpaSpecificationExecutor<StockMovement> {

    @Override
    @EntityGraph(attributePaths = "product")
    Page<StockMovement> findAll(Specification<StockMovement> spec, Pageable pageable);

    @Modifying
    @Query("delete from StockMovement m where m.product.id = :productId")
    void deleteByProductId(Long productId);
}
