package ru.practicum.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/categories")
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategoryAdmin(@Valid @RequestBody NewCategoryDto dto) {
        return categoryService.createCategoryAdmin(dto);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategoryAdmin(@PathVariable Long categoryId) {
        categoryService.deleteCategoryAdmin(categoryId);
    }

    @PatchMapping("/{categoryId}")
    public CategoryDto updateCategoryAdmin(@Valid @RequestBody CategoryDto dto, @PathVariable Long categoryId) {
        return categoryService.updateCategoryAdmin(dto, categoryId);
    }
}
