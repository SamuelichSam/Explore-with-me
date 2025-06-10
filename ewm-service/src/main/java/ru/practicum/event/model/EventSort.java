package ru.practicum.event.model;

import org.springframework.data.domain.Sort;

public enum EventSort {
    EVENT_DATE,
    VIEWS,
    RATING;

    public Sort getSort() {
        return switch (this) {
            case EVENT_DATE -> Sort.by(Sort.Direction.DESC, "eventDate");
            case VIEWS -> Sort.by(Sort.Direction.DESC, "views");
            case RATING -> Sort.by(Sort.Direction.DESC, "rating");
        };
    }
}
