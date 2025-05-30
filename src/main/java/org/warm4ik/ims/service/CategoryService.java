package org.warm4ik.ims.service;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.warm4ik.ims.dto.category.CategoryCreateDto;
import org.warm4ik.ims.dto.category.CategoryResponseDto;
import org.warm4ik.ims.dto.category.CategoryUpdateDto;
import org.warm4ik.ims.entity.Category;

import java.util.List;

public interface CategoryService {

  CategoryResponseDto findById(Long id);

  Page<CategoryResponseDto> findAll(int pageNumber);

  CategoryResponseDto create(CategoryCreateDto category);

  void deleteById(Long id);

  void update(CategoryUpdateDto category);

  CategoryResponseDto findByTitle(String title);

  Page<CategoryResponseDto> findByTitleContaining(String title, int pageNumber);

  Category getEntityById(Long id);

  List<CategoryResponseDto> findTitlesContaining(@NotBlank String searchStr);
}
