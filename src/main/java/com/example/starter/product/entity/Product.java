package com.example.starter.product.entity;

import com.example.starter.category.entity.Category;
import com.example.starter.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 30)
    private String unit = "cái";

    /** Selling price. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    /** Moving-average purchase cost, updated by stock receipts. */
    @Column(name = "cost_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal costPrice = BigDecimal.ZERO;

    /** Stock on hand. Only changed through stock movements (receipt, sale, cancel, adjustment). */
    @Column(nullable = false)
    private Integer quantity = 0;

    /** Low-stock threshold: quantity <= minStock is flagged. */
    @Column(name = "min_stock", nullable = false)
    private Integer minStock = 0;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;

    public boolean isLowStock() {
        return quantity != null && minStock != null && quantity <= minStock;
    }
}
