package org.warm4ik.ims.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.warm4ik.ims.dto.internship.InternshipResponseDto;
import org.warm4ik.ims.service.FavoriteService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ims/users/{userId}/favorites")
@Validated
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class FavoriteController {

  private final FavoriteService favoriteServiceImpl;

  @PostMapping("/{internshipId}")
  public ResponseEntity<Void> addFavorite(
      @PathVariable(value = "userId") @Min(1L) Long userId,
      @PathVariable(value = "internshipId") @Min(1L) Long internshipId) {

    favoriteServiceImpl.addFavorite(userId, internshipId);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @DeleteMapping("/{internshipId}")
  public ResponseEntity<Void> deleteFavorite(
      @PathVariable(value = "userId") @Min(1L) Long userId,
      @PathVariable(value = "internshipId") @Min(1L) Long internshipId) {

    favoriteServiceImpl.deleteFavorite(userId, internshipId);

    return ResponseEntity.noContent().build();
  }

  @GetMapping()
  public ResponseEntity<Page<InternshipResponseDto>> getAll(
      @PathVariable(value = "userId") @Min(1L) Long userId,
      @RequestParam(defaultValue = "0") @Min(0) int pageNumber) {

    Page<InternshipResponseDto> internships = favoriteServiceImpl.getAll(userId, pageNumber);

    return ResponseEntity.ok(internships);
  }
}
