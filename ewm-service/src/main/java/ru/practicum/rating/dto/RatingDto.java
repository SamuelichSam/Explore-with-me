package ru.practicum.rating.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record RatingDto(
        Long id,
        Long eventId,
        Long userId,
        Boolean liked,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime created
) {
}
