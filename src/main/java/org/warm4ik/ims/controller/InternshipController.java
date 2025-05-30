package org.warm4ik.ims.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.warm4ik.ims.dto.internship.InternshipCreateDto;
import org.warm4ik.ims.dto.internship.InternshipResponseDto;
import org.warm4ik.ims.dto.internship.InternshipSearchFilterDto;
import org.warm4ik.ims.dto.internship.InternshipUpdateDto;
import org.warm4ik.ims.service.InternshipService;

@RestController()
@RequestMapping("/ims/internships")
@RequiredArgsConstructor
@Validated
public class InternshipController {

  private final InternshipService internshipServiceImpl;

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<InternshipResponseDto> getById(@PathVariable("id") @Min(1L) Long id) {

    InternshipResponseDto internship = internshipServiceImpl.getById(id);

    return ResponseEntity.ok(internship);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteById(@PathVariable("id") @Min(1L) Long id) {

    internshipServiceImpl.deleteById(id);

    return ResponseEntity.noContent().build();
  }

  @GetMapping()
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<Page<InternshipResponseDto>> getAll(
      @RequestParam(defaultValue = "0") @Min(0) int pageNumber) {

    Page<InternshipResponseDto> internships = internshipServiceImpl.findAll(pageNumber);

    return ResponseEntity.ok(internships);
  }

  @PostMapping("/search")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<Page<InternshipResponseDto>> search(
      @RequestBody @Valid InternshipSearchFilterDto searchFilterInternshipDto,
      @RequestParam(defaultValue = "0") @Min(0) int pageNumber) {

    Page<InternshipResponseDto> internships =
        internshipServiceImpl.searchByParam(searchFilterInternshipDto, pageNumber);

    return ResponseEntity.ok(internships);
  }

  @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<InternshipResponseDto> create(
      @RequestPart("dto") @Valid InternshipCreateDto internshipCreateDto,
      @RequestPart(value = "logoImg", required = false) MultipartFile logoImg) {

    InternshipResponseDto responseDto = internshipServiceImpl.create(internshipCreateDto, logoImg);

    return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
  }

  @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> update(
      @RequestPart("dto") @Valid InternshipUpdateDto internshipUpdateDto,
      @RequestPart(value = "logoImg", required = false) MultipartFile logoImg) {

    internshipServiceImpl.update(internshipUpdateDto, logoImg);

    return ResponseEntity.noContent().build();
  }
}
