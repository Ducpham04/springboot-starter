package com.example.starter.product.repository;

import com.example.starter.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    long countByCategoryId(Long categoryId);

    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findWithCategoryById(Long id);

    /**
     * Locks the rows (SELECT ... FOR UPDATE) in id order so concurrent sales/receipts touching
     * the same products serialize without deadlocks.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids order by p.id")
    List<Product> findAllByIdForUpdate(Collection<Long> ids);

    @Query(value = """
            select (exists(select 1 from order_items where product_id = :id)
                 or exists(select 1 from stock_receipt_items where product_id = :id))
            """, nativeQuery = true)
    long hasTransactions(Long id);

    @Query("select count(p) from Product p where p.status = 'ACTIVE' and p.quantity <= p.minStock")
    long countLowStock();

    @EntityGraph(attributePaths = "category")
    @Query("select p from Product p where p.status = 'ACTIVE' and p.quantity <= p.minStock order by p.quantity asc, p.name asc")
    List<Product> findLowStock(Pageable pageable);
}
