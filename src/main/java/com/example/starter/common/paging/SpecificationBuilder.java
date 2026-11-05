package com.example.starter.common.paging;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Fluent, null-safe builder for dynamic JPA filters.
 * <p>
 * Every method ignores {@code null}/blank values, so you can pass request
 * parameters straight in without {@code if} checks:
 *
 * <pre>{@code
 * Specification<Product> spec = SpecificationBuilder.<Product>create()
 *         .likeAny(filter.keyword(), "name", "sku")
 *         .equal("status", filter.status())
 *         .between("price", filter.minPrice(), filter.maxPrice())
 *         .in("category", filter.categories())
 *         .build();
 * }</pre>
 * Attribute names may be nested with dots (e.g. {@code "category.name"}); joins are created as LEFT joins.
 */
public final class SpecificationBuilder<T> {

    private final List<Specification<T>> specs = new ArrayList<>();

    private SpecificationBuilder() {
    }

    public static <T> SpecificationBuilder<T> create() {
        return new SpecificationBuilder<>();
    }

    public SpecificationBuilder<T> equal(String attribute, Object value) {
        if (isPresent(value)) {
            specs.add((root, q, cb) -> cb.equal(path(root, attribute), value));
        }
        return this;
    }

    public SpecificationBuilder<T> notEqual(String attribute, Object value) {
        if (isPresent(value)) {
            specs.add((root, q, cb) -> cb.notEqual(path(root, attribute), value));
        }
        return this;
    }

    /** Case-insensitive "contains" match. */
    public SpecificationBuilder<T> like(String attribute, String value) {
        if (StringUtils.hasText(value)) {
            String pattern = likePattern(value);
            specs.add((root, q, cb) -> cb.like(cb.lower(path(root, attribute)), pattern, '\\'));
        }
        return this;
    }

    /** Case-insensitive "contains" match on any of the given attributes (OR). */
    public SpecificationBuilder<T> likeAny(String value, String... attributes) {
        if (StringUtils.hasText(value) && attributes.length > 0) {
            String pattern = likePattern(value);
            specs.add((root, q, cb) -> cb.or(java.util.Arrays.stream(attributes)
                    .map(a -> cb.like(cb.lower(path(root, a)), pattern, '\\'))
                    .toArray(jakarta.persistence.criteria.Predicate[]::new)));
        }
        return this;
    }

    public SpecificationBuilder<T> in(String attribute, Collection<?> values) {
        if (values != null && !values.isEmpty()) {
            specs.add((root, q, cb) -> path(root, attribute).in(values));
        }
        return this;
    }

    public <Y extends Comparable<? super Y>> SpecificationBuilder<T> greaterThanOrEqual(String attribute, Y value) {
        if (value != null) {
            specs.add((root, q, cb) -> cb.greaterThanOrEqualTo(path(root, attribute), value));
        }
        return this;
    }

    public <Y extends Comparable<? super Y>> SpecificationBuilder<T> lessThanOrEqual(String attribute, Y value) {
        if (value != null) {
            specs.add((root, q, cb) -> cb.lessThanOrEqualTo(path(root, attribute), value));
        }
        return this;
    }

    /** Inclusive range; either bound may be null (open range). */
    public <Y extends Comparable<? super Y>> SpecificationBuilder<T> between(String attribute, Y from, Y to) {
        return greaterThanOrEqual(attribute, from).lessThanOrEqual(attribute, to);
    }

    public SpecificationBuilder<T> isTrue(String attribute, Boolean apply) {
        if (Boolean.TRUE.equals(apply)) {
            specs.add((root, q, cb) -> cb.isTrue(path(root, attribute)));
        }
        return this;
    }

    /** Escape hatch for anything the builder does not cover. */
    public SpecificationBuilder<T> and(Specification<T> spec) {
        if (spec != null) {
            specs.add(spec);
        }
        return this;
    }

    public Specification<T> build() {
        return Specification.allOf(specs);
    }

    @SuppressWarnings("unchecked")
    private static <Y> Path<Y> path(Root<?> root, String attribute) {
        String[] parts = attribute.split("\\.");
        From<?, ?> from = root;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            From<?, ?> current = from;
            from = current.getJoins().stream()
                    .filter(j -> j.getAttribute().getName().equals(part))
                    .findFirst()
                    .map(j -> (From<?, ?>) j)
                    .orElseGet(() -> current.join(part, JoinType.LEFT));
        }
        return (Path<Y>) from.get(parts[parts.length - 1]);
    }

    private static String likePattern(String value) {
        String escaped = value.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    private static boolean isPresent(Object value) {
        return value != null && !(value instanceof String s && !StringUtils.hasText(s));
    }
}
