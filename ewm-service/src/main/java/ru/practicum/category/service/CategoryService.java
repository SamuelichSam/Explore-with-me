package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategorySearchDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategoryAdmin(NewCategoryDto dto);

    void deleteCategoryAdmin(Long categoryId);

    CategoryDto updateCategoryAdmin(CategoryDto dto);

    List<CategoryDto> findCategoriesPublic(CategorySearchDto searchDto);

    CategoryDto findCategoryByIdPublic(Long categoryId);
}
