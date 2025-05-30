package org.warm4ik.ims.dto.security;

import jakarta.validation.constraints.NotBlank;

public record UserCredentialDto(@NotBlank String email, @NotBlank String password) {}
