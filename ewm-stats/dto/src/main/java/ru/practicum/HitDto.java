package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record HitDto(
        String app,
        String uri,
        String ip,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {
}
