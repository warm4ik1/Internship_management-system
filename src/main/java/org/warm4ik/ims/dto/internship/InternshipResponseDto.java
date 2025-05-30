package org.warm4ik.ims.dto.internship;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.warm4ik.ims.dto.category.CategoryResponseDto;
import org.warm4ik.ims.entity.enums.PaymentType;

import java.time.LocalDateTime;
import java.util.List;

public record InternshipResponseDto(
    @NotNull Long internshipId,
    @NotBlank String logoUrl,
    @NotBlank String title,
    @NotBlank String description,
    @NotNull PaymentType paymentType,
    @Valid @NotEmpty List<InternshipContactDataDto> contactData,
    @Valid @NotEmpty List<CategoryResponseDto> categoriesData,
    @NotBlank String address,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull LocalDateTime createdAt,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull LocalDateTime updatedAt) {}
