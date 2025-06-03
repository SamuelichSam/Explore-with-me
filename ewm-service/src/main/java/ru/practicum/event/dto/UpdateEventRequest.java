package ru.practicum.event.dto;

public interface UpdateEventRequest {
    String annotation();

    String description();

    String eventDate();

    Boolean paid();

    Integer participantLimit();

    Boolean requestModeration();

    String title();
}
