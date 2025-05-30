package org.warm4ik.ims.service;

import org.springframework.data.domain.Page;
import org.warm4ik.ims.dto.internship.InternshipResponseDto;

public interface FavoriteService {

  void addFavorite(Long userId, Long internshipId);

  void deleteFavorite(Long userId, Long internshipId);

  Page<InternshipResponseDto> getAll(Long userId, int pageNumber);
}
