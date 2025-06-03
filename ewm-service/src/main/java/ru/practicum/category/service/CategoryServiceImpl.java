package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategorySearchDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repo.CategoryRepository;
import ru.practicum.event.repo.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto createCategoryAdmin(NewCategoryDto dto) {
        log.info("Добавление новой категории");
        if (categoryRepository.existsByName(dto.name())) {
            throw new ConflictException("Категория с именем " + dto.name() + " уже существует");
        }
        Category category = categoryRepository.save(categoryMapper.toCategory(dto));
        return categoryMapper.toDto(category);
    }

    @Override
    public void deleteCategoryAdmin(Long categoryId) {
        log.info("Удаление категории с id - {}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена"));
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("Существуют события, связанные с категорией с id " + categoryId);
        }
        categoryRepository.delete(category);
    }

    @Override
    public CategoryDto updateCategoryAdmin(CategoryDto dto) {
        log.info("Обновление категории с id - {}", dto.id());
        Category category = categoryRepository.findById(dto.id())
                .orElseThrow(() -> new NotFoundException("Категория с id " + dto.id() + " не найдена"));
        if (dto.name() != null && !dto.name().equals(category.getName())) {
            if (categoryRepository.existsByName(dto.name())) {
                throw new ConflictException("Категория с именем " + dto.name() + " уже существует");
            }
            category.setName(dto.name());
        }
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> findCategoriesPublic(CategorySearchDto searchDto) {
        log.info("Получение категорий");
        Pageable pageable = PageRequest.of(searchDto.from() / searchDto.size(), searchDto.size());
        List<Category> categories = categoryRepository.findAll(pageable).getContent();
        return categories.stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto findCategoryByIdPublic(Long categoryId) {
        log.info("Получение информации о категории с id - {}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена"));
        return categoryMapper.toDto(category);
    }
}
