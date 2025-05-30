package org.warm4ik.ims.dto.security;

import jakarta.validation.constraints.NotBlank;

public record JwtAuthenticationDto(@NotBlank String token, @NotBlank String refreshToken) {

}
