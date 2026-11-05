package com.example.starter.category.repository;

import com.example.starter.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    /** Category id + number of products, in one query (no N+1). */
    @Query("select p.category.id, count(p) from Product p where p.category is not null group by p.category.id")
    List<Object[]> countProductsByCategory();
}
