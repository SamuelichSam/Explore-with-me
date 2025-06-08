package ru.practicum.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ru.practicum.event.model.Location;

public record NewEventDto(
        @NotBlank
        @Size(min = 20, max = 2000)
        String annotation,
        Long category,
        @NotBlank
        @Size(min = 20, max = 7000)
        String description,
        String eventDate,
        Location location,
        Boolean paid,
        @PositiveOrZero
        Integer participantLimit,
        Boolean requestModeration,
        @NotBlank
        @Size(min = 3, max = 120)
        String title,
        Integer rating
) {
    public NewEventDto {
        paid = paid != null ? paid : false;
        participantLimit = participantLimit != null ? participantLimit : 0;
        requestModeration = requestModeration != null ? requestModeration : true;
        rating = rating != null ? rating : 5;
    }
}
