package org.warm4ik.ims.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryUpdateDto(@NotNull Long categoryId, @NotBlank @Size(min = 2, max = 100) String title) {
}
