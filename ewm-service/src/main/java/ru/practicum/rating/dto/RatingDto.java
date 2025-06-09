package ru.practicum.rating.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.rating.model.RatingType;

import java.time.LocalDateTime;

public record RatingDto(
        Long id,
        Long eventId,
        Long userId,
        RatingType ratingType,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime created
) {
}
