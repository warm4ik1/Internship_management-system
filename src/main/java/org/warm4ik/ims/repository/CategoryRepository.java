package org.warm4ik.ims.repository;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.warm4ik.ims.entity.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  Category findByTitle(String title);

  Page<Category> findByTitleContaining(String title, Pageable pageable);

  @Query(
      """
    SELECT COUNT(i) > 0
    FROM Internship i
    JOIN i.categories c
    WHERE SIZE(i.categories) = 1
    AND c.id = :categoryId
    """)
  boolean isOnlyCategoryInAnyInternships(@Param("categoryId") Long categoryId);

  boolean existsByTitle(@NotBlank String title);

  List<Category> findByTitleContainingIgnoreCase(@NotBlank String searchStr, Pageable pageable);
}
