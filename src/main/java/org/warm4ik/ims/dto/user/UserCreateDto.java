package org.warm4ik.ims.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateDto(
    @NotBlank @Size(min = 8, max = 50) String username,
    @NotBlank @Size(min = 8, max = 50) String password,
    @NotBlank @Email @Size(max = 254) String email) {}
