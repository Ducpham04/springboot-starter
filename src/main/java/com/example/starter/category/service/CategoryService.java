package com.example.starter.category.service;

import com.example.starter.category.dto.CategoryRequest;
import com.example.starter.category.dto.CategoryResponse;
import com.example.starter.category.entity.Category;
import com.example.starter.category.repository.CategoryRepository;
import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<CategoryResponse> findAll() {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : categoryRepository.countProductsByCategory()) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return categoryRepository.findAll(Sort.by("name")).stream()
                .map(c -> toResponse(c, counts.getOrDefault(c.getId(), 0L)))
                .toList();
    }

    public Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTS, name);
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(request.description());
        return toResponse(categoryRepository.save(category), 0);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getEntity(id);
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTS, name);
        }
        category.setName(name);
        category.setDescription(request.description());
        return toResponse(category, productRepository.countByCategoryId(id));
    }

    @Transactional
    public void delete(Long id) {
        Category category = getEntity(id);
        if (productRepository.countByCategoryId(id) > 0) {
            throw new AppException(ErrorCode.CATEGORY_IN_USE);
        }
        categoryRepository.delete(category);
    }

    private static CategoryResponse toResponse(Category c, long productCount) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), productCount);
    }
}
