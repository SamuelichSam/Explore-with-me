package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NewCompilationDto(
        Long id,
        List<Long> events,
        Boolean pinned,
        @NotBlank
        @Size(min = 1, max = 50)
        String title
) {
        public NewCompilationDto {
                pinned = pinned != null ? pinned : false;
        }
}
