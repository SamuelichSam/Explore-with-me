package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record EventPublicRequestParams(
        String text,
        List<Long> categories,
        Boolean paid,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime rangeStart,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime rangeEnd,
        Boolean onlyAvailable,
        String sort,
        Integer from,
        Integer size
) {

    public static EventPublicRequestParams of(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            String sort,
            Integer from,
            Integer size
    ) {
        return new EventPublicRequestParams(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable != null ? onlyAvailable : false,
                sort,
                from != null ? from : 0,
                size != null ? size : 10
        );
    }
}
