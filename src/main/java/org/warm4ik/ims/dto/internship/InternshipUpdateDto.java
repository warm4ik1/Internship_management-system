package org.warm4ik.ims.dto.internship;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.warm4ik.ims.entity.enums.PaymentType;

import java.util.List;

public record InternshipUpdateDto(
    @NotNull @Min(1) Long internshipId,
    @NotBlank String logoUrl,
    @NotBlank @Size(min = 5, max = 100) String title,
    @NotBlank @Size(min = 250, max = 2500) String description,
    @NotNull PaymentType paymentType,
    @Valid @NotEmpty List<InternshipContactDataDto> contactData,
    @NotEmpty @Valid List<@NotNull Long> categoriesIds,
    @NotBlank @Size(min = 2, max = 100) String address) {}

