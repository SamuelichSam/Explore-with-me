package ru.practicum.category.dto;

public record CategorySearchDto(
        Integer from,
        Integer size
) {

    public static CategorySearchDto of(
            Integer from,
            Integer size
    ) {
        return new CategorySearchDto(
                from != null ? from : 0,
                size != null ? size : 10
        );
    }
}
