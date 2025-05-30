package org.warm4ik.ims.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.warm4ik.ims.dto.internship.InternshipResponseDto;
import org.warm4ik.ims.entity.Internship;
import org.warm4ik.ims.entity.User;
import org.warm4ik.ims.exception.custom.ValidationException;
import org.warm4ik.ims.service.FavoriteService;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

  private final InternshipServiceImpl internshipServiceImpl;
  private final UserServiceImpl userServiceImpl;

  @Value("${pagination.default-page-size}")
  private int DEFAULT_PAGE_SIZE;

  @Transactional
  @Override
  public void addFavorite(Long userId, Long internshipId) {

    User user = userServiceImpl.getEntityById(userId);
    Internship internship = internshipServiceImpl.getEntityById(internshipId);

    if (!user.getFavoriteInternships().contains(internship)) {
      user.getFavoriteInternships().add(internship);
    } else {
      throw new ValidationException("Данная стажировка уже добавлена в избранное!");
    }

    userServiceImpl.save(user);
  }

  @Transactional
  @Override
  public void deleteFavorite(Long userId, Long internshipId) {

    User user = userServiceImpl.getEntityById(userId);
    Internship internship = internshipServiceImpl.getEntityById(internshipId);

    user.getFavoriteInternships().remove(internship);

    userServiceImpl.save(user);
  }

  @Transactional(readOnly = true)
  @Override
  public Page<InternshipResponseDto> getAll(Long userId, int pageNumber) {

    Pageable pageable = PageRequest.of(pageNumber, DEFAULT_PAGE_SIZE);
    return internshipServiceImpl.findFavoritesForUser(userId, pageable);
  }
}
