package org.warm4ik.ims.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.warm4ik.ims.dto.category.CategoryCreateDto;
import org.warm4ik.ims.dto.category.CategoryResponseDto;

import org.warm4ik.ims.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

  Category categoryCreateDtoToCategory(CategoryCreateDto categoryCreateDto);

  @Mapping(source = "id", target = "categoryId")
  CategoryResponseDto categoryToCategoryResponseDto(Category category);
}
