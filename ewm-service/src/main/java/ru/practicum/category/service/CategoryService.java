package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategoryAdmin(NewCategoryDto dto);

    void deleteCategoryAdmin(Long categoryId);

    CategoryDto updateCategoryAdmin(CategoryDto dto, Long categoryId);

    List<CategoryDto> findCategoriesPublic(Integer from, Integer size);

    CategoryDto findCategoryByIdPublic(Long categoryId);
}
