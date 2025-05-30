package org.warm4ik.ims.dto.internship;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.warm4ik.ims.entity.enums.PaymentType;

import java.time.LocalDateTime;
import java.util.List;

public record InternshipSearchFilterDto(
    @NotBlank @Pattern(
            regexp = "title|description",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "флаг поиска должен быть одним из: title, description") String mode,
    @NotBlank @Size(min = 2) String searchStr,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime start,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime end,
    String address,
    List<String> categoryNames,
    PaymentType paymentType) {}
