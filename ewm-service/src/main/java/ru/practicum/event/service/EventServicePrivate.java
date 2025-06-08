package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface EventServicePrivate {
    List<EventShortDto> findEventsPrivate(Long userId, Integer from, Integer size);

    EventFullDto createEventPrivate(Long userId, NewEventDto dto);

    EventFullDto findEventByIdPrivate(Long userId, Long eventId);

    EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest dto);

    List<ParticipationRequestDto> findEventRequestsPrivate(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequestsPrivate(Long userId,
                                                              Long eventId,
                                                              EventRequestStatusUpdateRequest dto);

    EventFullDto findEventRatingPrivate(Long userId, Long eventId);
}
