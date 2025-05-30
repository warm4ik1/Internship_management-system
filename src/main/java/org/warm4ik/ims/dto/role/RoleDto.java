package org.warm4ik.ims.dto.role;

import jakarta.validation.constraints.NotBlank;

public record RoleDto(@NotBlank String title) {}
