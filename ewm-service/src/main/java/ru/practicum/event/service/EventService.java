package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.dto.*;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventShortDto> findEventsPrivate(Long userId, Integer from, Integer size);

    EventFullDto createEventPrivate(Long userId, NewEventDto dto);

    EventFullDto findEventByIdPrivate(Long userId, Long eventId);

    EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest dto);

    List<ParticipationRequestDto> findEventRequestsPrivate(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequestsPrivate(Long userId,
                                                               Long eventId,
                                                               EventRequestStatusUpdateRequest dto);

    List<EventFullDto> findEventsAdmin(List<Long> users, List<String> states, List<Long> categories,
                                       LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest dto);

    List<EventShortDto> findEventsPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                         Integer size, HttpServletRequest request);

    EventFullDto findEventByIdPublic(Long eventId, HttpServletRequest request);
}
