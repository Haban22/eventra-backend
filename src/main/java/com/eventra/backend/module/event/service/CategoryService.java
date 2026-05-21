package com.eventra.backend.module.event.service;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.event.dto.request.CategoryRequest;
import com.eventra.backend.module.event.dto.response.CategoryResponse;
import com.eventra.backend.module.event.entity.Category;
import com.eventra.backend.module.event.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "CATEGORY_EXISTS", "A category with this name already exists");
        }
        Category category = new Category();
        category.setName(request.name().trim());
        category.setIcon(request.icon());
        category.setDescription(request.description());
        return CategoryResponse.from(categoryRepository.save(category));
    }
}