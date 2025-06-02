package ru.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.request.model.Status;

import java.time.LocalDateTime;

public record ParticipationRequestDto(
        Long id,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime created,
        Long event,
        Long requester,
        Status status
) {
}
