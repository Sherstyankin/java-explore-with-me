package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.request.NewCategoryDto;
import ru.practicum.entity.Category;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CategoryMapper {

    public Category mapToCategory(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public CategoryDto mapToCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public List<CategoryDto> mapToCategoryDto(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }
        return categories.stream()
                .map(CategoryMapper::mapToCategoryDto)
                .collect(Collectors.toList());
    }
}
