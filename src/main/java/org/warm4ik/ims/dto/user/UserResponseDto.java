package org.warm4ik.ims.dto.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.warm4ik.ims.dto.role.RoleDto;

import java.util.List;

public record UserResponseDto(@NotNull @Min(1) Long id,
                              @NotNull String username,
                              @NotNull String email,
                              @Valid @NotEmpty List<RoleDto> roles) {
}
