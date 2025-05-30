package org.warm4ik.ims.dto.security;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDto(@NotBlank String refreshToken) {}
