package ru.practicum.category.service;

import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.request.NewCategoryDto;
import ru.practicum.entity.Category;

import java.util.List;

public interface CategoryService {
    CategoryDto addNewCategory(NewCategoryDto dto);

    void deleteCategory(Long catId);

    CategoryDto updateCategory(Long catId, NewCategoryDto dto);

    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto getCategoryById(Long catId);

    Category findCategoryById(Long catId);
}
