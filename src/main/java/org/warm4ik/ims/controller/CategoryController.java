package org.warm4ik.ims.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.warm4ik.ims.dto.category.CategoryCreateDto;
import org.warm4ik.ims.dto.category.CategoryResponseDto;
import org.warm4ik.ims.dto.category.CategoryUpdateDto;
import org.warm4ik.ims.service.CategoryService;

import java.util.List;

@RestController()
@RequestMapping("/ims/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

  private final CategoryService categoryServiceImpl;

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CategoryResponseDto> getById(@PathVariable("id") @Min(1L) Long id) {

    CategoryResponseDto category = categoryServiceImpl.findById(id);

    return ResponseEntity.ok(category);
  }

  @GetMapping()
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<CategoryResponseDto>> getAll(
      @RequestParam(defaultValue = "0") @Min(0) int pageNumber) {

    Page<CategoryResponseDto> categories = categoryServiceImpl.findAll(pageNumber);

    return ResponseEntity.ok(categories);
  }

  @PostMapping()
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CategoryResponseDto> create(
      @RequestBody @Valid CategoryCreateDto category) {

    CategoryResponseDto createdCategory = categoryServiceImpl.create(category);

    return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteById(@PathVariable("id") @Min(1L) Long id) {

    categoryServiceImpl.deleteById(id);

    return ResponseEntity.noContent().build();
  }

  @PutMapping()
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> updateCategory(@RequestBody @Valid CategoryUpdateDto category) {

    categoryServiceImpl.update(category);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/search")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CategoryResponseDto> searchCategory(@RequestParam @NotBlank String title) {

    CategoryResponseDto foundCategory = categoryServiceImpl.findByTitle(title);

    return ResponseEntity.ok(foundCategory);
  }

  @GetMapping("/search/like")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<CategoryResponseDto>> searchCategories(
      @RequestParam @NotBlank String title,
      @RequestParam(defaultValue = "0") @Min(0) int pageNumber) {

    Page<CategoryResponseDto> categories =
        categoryServiceImpl.findByTitleContaining(title, pageNumber);

    return ResponseEntity.ok(categories);
  }

  @GetMapping("/autocomplete")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<List<CategoryResponseDto>> autocomplete(
      @RequestParam @NotBlank String searchStr) {

    List<CategoryResponseDto> items = categoryServiceImpl.findTitlesContaining(searchStr);
    return ResponseEntity.ok(items);
  }
}
