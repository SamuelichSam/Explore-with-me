package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record EventAdminRequestParams(
        List<Long> users,
        List<String> states,
        List<Long> categories,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime rangeStart,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime rangeEnd,
        Integer from,
        Integer size
) {

    public static EventAdminRequestParams of(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Integer from,
            Integer size
    ) {
        return new EventAdminRequestParams(
                users,
                states,
                categories,
                rangeStart,
                rangeEnd,
                from != null ? from : 0,
                size != null ? size : 10
        );
    }
}
