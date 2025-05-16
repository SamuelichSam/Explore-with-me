package ru.practicum;

public record StatDto(
        String app,
        String uri,
        Long hits
) {
}
