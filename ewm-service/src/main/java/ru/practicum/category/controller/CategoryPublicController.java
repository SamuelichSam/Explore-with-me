package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategorySearchDto;
import ru.practicum.category.service.CategoryService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoryPublicController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> findCategoriesPublic(@RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "10") Integer size) {
        var params = CategorySearchDto.of(from, size);
        return categoryService.findCategoriesPublic(params);
    }

    @GetMapping("/{categoryId}")
    public CategoryDto findCategoryByIdPublic(@PathVariable Long categoryId) {
        return categoryService.findCategoryByIdPublic(categoryId);
    }
}
