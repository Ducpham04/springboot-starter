package com.example.starter.order.repository;

import com.example.starter.order.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<SalesOrder, Long>, JpaSpecificationExecutor<SalesOrder> {

    @Override
    @EntityGraph(attributePaths = {"customer", "seller"})
    Page<SalesOrder> findAll(Specification<SalesOrder> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "seller", "items"})
    Optional<SalesOrder> findDetailById(Long id);

    boolean existsByCustomerId(Long customerId);

    /** customerId, order count, total spent, last order time (completed orders only). */
    @Query("""
            select o.customer.id, count(o), sum(o.total), max(o.createdAt)
            from SalesOrder o
            where o.status = com.example.starter.order.entity.OrderStatus.COMPLETED
              and o.customer.id in :customerIds
            group by o.customer.id
            """)
    List<Object[]> customerStats(Collection<Long> customerIds);

    // ---- Dashboard aggregates (completed orders only) ----

    /** [count, revenue, cost] in the period. */
    @Query("""
            select count(o), coalesce(sum(o.total), 0), coalesce(sum(o.totalCost), 0)
            from SalesOrder o
            where o.status = com.example.starter.order.entity.OrderStatus.COMPLETED
              and o.createdAt >= :from and o.createdAt < :to
            """)
    List<Object[]> totals(Instant from, Instant to);

    /** Raw rows for daily revenue: created_at, total (period is at most a few weeks). */
    @Query("""
            select o.createdAt, o.total from SalesOrder o
            where o.status = com.example.starter.order.entity.OrderStatus.COMPLETED
              and o.createdAt >= :from and o.createdAt < :to
            """)
    List<Object[]> revenueRows(Instant from, Instant to);

    /** productId, name, quantity sold, revenue. */
    @Query("""
            select i.product.id, i.productName, sum(i.quantity), sum(i.lineTotal)
            from OrderItem i
            where i.order.status = com.example.starter.order.entity.OrderStatus.COMPLETED
              and i.order.createdAt >= :from and i.order.createdAt < :to
            group by i.product.id, i.productName
            order by sum(i.quantity) desc
            """)
    List<Object[]> topProducts(Instant from, Instant to, Pageable pageable);
}
