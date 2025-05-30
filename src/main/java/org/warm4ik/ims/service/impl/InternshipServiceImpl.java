package org.warm4ik.ims.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.warm4ik.ims.dto.internship.InternshipCreateDto;
import org.warm4ik.ims.dto.internship.InternshipResponseDto;
import org.warm4ik.ims.dto.internship.InternshipSearchFilterDto;
import org.warm4ik.ims.dto.internship.InternshipUpdateDto;
import org.warm4ik.ims.entity.Category;
import org.warm4ik.ims.entity.Internship;
import org.warm4ik.ims.entity.InternshipContactData;
import org.warm4ik.ims.exception.custom.ResourceNotFoundException;
import org.warm4ik.ims.exception.custom.ValidationException;
import org.warm4ik.ims.repository.specification.InternshipSpecification;
import org.warm4ik.ims.mapper.ContactDataMapper;
import org.warm4ik.ims.mapper.InternshipMapper;
import org.warm4ik.ims.repository.InternshipRepository;
import org.warm4ik.ims.service.InternshipService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InternshipServiceImpl implements InternshipService {

  private final InternshipRepository internshipRepository;

  private final FileStorageServiceImpl fileStorageServiceImpl;
  private final CategoryServiceImpl categoryServiceImpl;

  private final InternshipMapper internshipMapper;
  private final ContactDataMapper contactDataMapper;

  @Value("${pagination.default-page-size}")
  private int DEFAULT_PAGE_SIZE;

  @Transactional(readOnly = true)
  @Override
  public InternshipResponseDto getById(Long id) {

    Internship internship =
        internshipRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Cтажировка с id: " + id + " не найдена"));

    return internshipMapper.internshipToResponseInternshipDto(internship);
  }

  @Transactional
  @Override
  public void deleteById(Long id) {

    Internship internship =
        internshipRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Cтажировка с id: " + id + " не найдена"));

    internshipRepository.deleteById(id);

    fileStorageServiceImpl.deleteImgFromFileDb(internship.getLogoUrl());
  }

  @Transactional(readOnly = true)
  @Override
  public Page<InternshipResponseDto> findAll(int pageNumber) {

    Pageable pageable = PageRequest.of(pageNumber, DEFAULT_PAGE_SIZE, Sort.by("id").descending());

    return internshipRepository
        .findAll(pageable)
        .map(internshipMapper::internshipToResponseInternshipDto);
  }

  @Transactional(readOnly = true)
  @Override
  public Page<InternshipResponseDto> findFavoritesForUser(Long userId, Pageable pageable) {

    return internshipRepository
        .findFavoritesByUserId(userId, pageable)
        .map(internshipMapper::internshipToResponseInternshipDto);
  }

  @Transactional(readOnly = true)
  @Override
  public Page<InternshipResponseDto> searchByParam(
      InternshipSearchFilterDto filterDto, int pageNumber) {

    Pageable pageable = PageRequest.of(pageNumber, DEFAULT_PAGE_SIZE, Sort.by("id").descending());

    Specification<Internship> spec =
        Specification.where(InternshipSpecification.byMode(filterDto.mode(), filterDto.searchStr()))
            .and(InternshipSpecification.betweenData(filterDto.start(), filterDto.end()))
            .and(InternshipSpecification.hasAddress(filterDto.address()))
            .and(InternshipSpecification.hasCategories(filterDto.categoryNames()))
            .and(InternshipSpecification.hasPaymentType(filterDto.paymentType()));

    return internshipRepository
        .findAll(spec, pageable)
        .map(internshipMapper::internshipToResponseInternshipDto);
  }

  @Transactional
  @Override
  public InternshipResponseDto create(
      InternshipCreateDto internshipCreateDto, MultipartFile logoImg) {

    if (internshipCreateDto == null) {
      throw new ValidationException("Пустые данные. Невозможно выполнить операцию сохранения!");
    }

    List<InternshipContactData> contacts =
        internshipCreateDto.contactData().stream()
            .map(contactDataMapper::internshipContactDataDtoToInternshipContactData)
            .toList();

    String newLogoUrl;
    if (logoImg == null || logoImg.isEmpty()) {
      newLogoUrl = fileStorageServiceImpl.getDefaultLogo();
    } else {
      newLogoUrl = fileStorageServiceImpl.generateUrlLogo(logoImg);
    }

    List<Category> categories =
        internshipCreateDto.categoriesIds().stream()
            .map(categoryServiceImpl::getEntityById)
            .collect(Collectors.toList());

    Internship internship =
        Internship.builder()
            .title(internshipCreateDto.title())
            .description(internshipCreateDto.description())
            .paymentType(internshipCreateDto.paymentType())
            .logoUrl(newLogoUrl)
            .address(internshipCreateDto.address())
            .categories(categories)
            .createdAt(LocalDateTime.now())
            .build();

    for (InternshipContactData contact : contacts) {
      internship.addContactData(contact);
    }

    Internship savedInternship = internshipRepository.save(internship);

    if (logoImg != null && !logoImg.isEmpty()) {
      fileStorageServiceImpl.saveAvatarToFileDb(logoImg, newLogoUrl);
    }

    return internshipMapper.internshipToResponseInternshipDto(savedInternship);
  }

  @Transactional
  @Override
  public void update(InternshipUpdateDto internshipUpdateDto, MultipartFile logoImg) {

    Internship existingInternship =
        internshipRepository
            .findById(internshipUpdateDto.internshipId())
            .orElseThrow(() -> new ResourceNotFoundException("Стажировка не найдена"));

    List<InternshipContactData> contacts =
        internshipUpdateDto.contactData().stream()
            .map(contactDataMapper::internshipContactDataDtoToInternshipContactData)
            .collect(Collectors.toCollection(ArrayList::new));

    List<Category> categories =
        internshipUpdateDto.categoriesIds().stream()
            .map(categoryServiceImpl::getEntityById)
            .collect(Collectors.toCollection(ArrayList::new));

    String newLogoUrl = internshipUpdateDto.logoUrl(); // используем старый URL по умолчанию

    if (logoImg != null && !logoImg.isEmpty()) {
      newLogoUrl = fileStorageServiceImpl.generateUrlLogo(logoImg); // заранее получаем новый URL
      existingInternship.setLogoUrl(newLogoUrl); // обновляем только если файл есть
    } else {
      existingInternship.setLogoUrl(
          newLogoUrl); // всё равно сетим старый логотип, если не загружаем
    }

    existingInternship.setTitle(internshipUpdateDto.title());
    existingInternship.setDescription(internshipUpdateDto.description());
    existingInternship.setPaymentType(internshipUpdateDto.paymentType());

    existingInternship.clearContactData();
    internshipRepository.saveAndFlush(existingInternship);
    contacts.forEach(existingInternship::addContactData);

    existingInternship.setCategories(categories);
    existingInternship.setAddress(internshipUpdateDto.address());

    internshipRepository.save(existingInternship);

    // вне транзакции: пишем файл только если он реально был загружен
    if (logoImg != null && !logoImg.isEmpty()) {
      fileStorageServiceImpl.updateAvatarInFileDb(
          logoImg, internshipUpdateDto.logoUrl(), newLogoUrl);
    }
  }

  @Transactional(readOnly = true)
  @Override
  public Internship getEntityById(Long internshipId) {

    return internshipRepository
        .findById(internshipId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Cтажировка с id: " + internshipId + " не найдена"));
  }
}
