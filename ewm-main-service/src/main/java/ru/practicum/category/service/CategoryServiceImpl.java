package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.request.NewCategoryDto;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.mapper.CategoryMapper;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto addNewCategory(NewCategoryDto dto) {
        Category category = CategoryMapper.mapToCategory(dto);
        return CategoryMapper.mapToCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long catId) {
        findCategoryById(catId);
        Event event = eventRepository.findFirstByCategoryId(catId).orElse(null);
        if (event != null) {
            throw new ConflictException("Существует событие, которое связано с категорией");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, NewCategoryDto dto) {
        Category category = findCategoryById(catId);
        category.setName(dto.getName());
        return CategoryMapper.mapToCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable page = PageRequest.of(from, size, Sort.by("id"));
        return CategoryMapper.mapToCategoryDto(categoryRepository.findAll(page).getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long catId) {
        return CategoryMapper.mapToCategoryDto(findCategoryById(catId));
    }

    @Override
    public Category findCategoryById(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(() ->
                new EntityNotFoundException("Категория c ID:" + catId + " не найдена."));
    }
}
