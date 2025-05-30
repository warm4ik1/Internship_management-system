package org.warm4ik.ims.service.impl;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.warm4ik.ims.dto.category.CategoryCreateDto;
import org.warm4ik.ims.dto.category.CategoryResponseDto;
import org.warm4ik.ims.dto.category.CategoryUpdateDto;
import org.warm4ik.ims.entity.Category;
import org.warm4ik.ims.exception.custom.DeleteWithRelationException;
import org.warm4ik.ims.exception.custom.ResourceNotFoundException;
import org.warm4ik.ims.exception.custom.ValidationException;
import org.warm4ik.ims.mapper.CategoryMapper;
import org.warm4ik.ims.repository.CategoryRepository;
import org.warm4ik.ims.service.CategoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  @Value("${pagination.default-page-size}")
  private int DEFAULT_PAGE_SIZE;

  @Transactional(readOnly = true)
  public CategoryResponseDto findById(Long id) {
    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Категория с id: " + id + " не найдена"));

    return categoryMapper.categoryToCategoryResponseDto(category);
  }

  @Transactional(readOnly = true)
  public Page<CategoryResponseDto> findAll(int pageNumber) {

    Pageable pageable = PageRequest.of(pageNumber, DEFAULT_PAGE_SIZE, Sort.by("id").descending());

    return categoryRepository.findAll(pageable).map(categoryMapper::categoryToCategoryResponseDto);
  }

  @Transactional
  public CategoryResponseDto create(CategoryCreateDto category) {

    if (categoryRepository.existsByTitle(category.title())) {
      throw new ValidationException("Категория с данным названием уже сущ-ет!");
    }

    if (category.title() != null) {
      Category newCategory = categoryMapper.categoryCreateDtoToCategory(category);

      categoryRepository.save(newCategory);

      return categoryMapper.categoryToCategoryResponseDto(newCategory);
    } else {
      throw new ValidationException("Поле title не может быть пустым.");
    }
  }

  @Transactional
  public void deleteById(Long id) {

    if (categoryRepository.isOnlyCategoryInAnyInternships(id)) {
      throw new DeleteWithRelationException(
          "Категория является единственной у какой-то из стажировок и не может быть удалена");
    }

    try {
      categoryRepository.deleteById(id);
    } catch (EmptyResultDataAccessException ex) {
      throw new ResourceNotFoundException(
          "Категория с id " + id + " не найдена и не может быть удалена.");
    }
  }

  @Transactional
  public void update(CategoryUpdateDto category) {

    if (categoryRepository.existsByTitle(category.title())) {
      throw new ValidationException("Категория с данным названием уже сущ-ет!");
    }

    Category existingCategory =
        categoryRepository
            .findById(category.categoryId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Категория не найдена, невозможно выполнить обновление"));

    existingCategory.setTitle(category.title());

    categoryRepository.save(existingCategory);
  }

  @Transactional(readOnly = true)
  public CategoryResponseDto findByTitle(String title) {

    Category category = categoryRepository.findByTitle(title);

    if (category == null) {
      throw new ResourceNotFoundException("Категорий по заданным параметрам не найдено");
    }

    return categoryMapper.categoryToCategoryResponseDto(category);
  }

  public Page<CategoryResponseDto> findByTitleContaining(String title, int pageNumber) {

    Pageable pageable = PageRequest.of(pageNumber, DEFAULT_PAGE_SIZE, Sort.by("title").ascending());

    return categoryRepository
        .findByTitleContaining(title, pageable)
        .map(categoryMapper::categoryToCategoryResponseDto);
  }

  @Transactional(readOnly = true)
  public Category getEntityById(Long id) {
    return categoryRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Категория с id: " + id + " не найдена"));
  }

  @Transactional(readOnly = true)
  public List<CategoryResponseDto> findTitlesContaining(@NotBlank String searchStr) {
    Pageable top10 = PageRequest.of(0, 10, Sort.by("title").ascending());

    return categoryRepository.findByTitleContainingIgnoreCase(searchStr, top10).stream()
        .map(c -> new CategoryResponseDto(c.getId(), c.getTitle()))
        .toList();
  }
}
