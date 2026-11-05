package com.example.starter.common;

import com.example.starter.common.paging.SpecificationBuilder;
import com.example.starter.product.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpecificationBuilderTest {

    @Test
    void nullAndBlankValuesProduceNoPredicates() {
        Specification<Product> spec = SpecificationBuilder.<Product>create()
                .equal("status", null)
                .like("name", "  ")
                .likeAny(null, "name", "sku")
                .in("category", List.of())
                .between("price", null, null)
                .build();

        // An empty conjunction yields a null predicate (= no WHERE clause)
        assertThat(spec.toPredicate(null, null, null)).isNull();
    }
}
