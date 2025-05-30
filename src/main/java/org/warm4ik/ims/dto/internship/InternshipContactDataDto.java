package org.warm4ik.ims.dto.internship;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.warm4ik.ims.dto.validation.ContactDataValid;

@ContactDataValid
public record InternshipContactDataDto(
    @NotBlank(message = "Тип контакта обязателен")
        @Pattern(
            regexp = "TELEGRAM|EMAIL|WEBSITE",
            message = "Тип контакта должен быть одним из: TELEGRAM, EMAIL, WEBSITE")
        String type,
    @NotBlank(message = "Значение контакта обязательно") String value) {}
