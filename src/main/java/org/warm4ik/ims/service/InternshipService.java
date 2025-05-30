package org.warm4ik.ims.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.warm4ik.ims.dto.internship.InternshipCreateDto;
import org.warm4ik.ims.dto.internship.InternshipResponseDto;
import org.warm4ik.ims.dto.internship.InternshipSearchFilterDto;
import org.warm4ik.ims.dto.internship.InternshipUpdateDto;
import org.warm4ik.ims.entity.Internship;

public interface InternshipService {

  InternshipResponseDto getById(Long id);

  void deleteById(Long id);

  Page<InternshipResponseDto> findAll(int pageNumber);

  Page<InternshipResponseDto> findFavoritesForUser(Long userId, Pageable pageable);

  Page<InternshipResponseDto> searchByParam(InternshipSearchFilterDto filterDto, int pageNumber);

  InternshipResponseDto create(InternshipCreateDto internshipCreateDto, MultipartFile logoImg);

  void update(InternshipUpdateDto internshipUpdateDto, MultipartFile logoImg);

  Internship getEntityById(Long internshipId);
}
