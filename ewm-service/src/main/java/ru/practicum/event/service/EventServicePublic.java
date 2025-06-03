package ru.practicum.event.service;

import ru.practicum.event.dto.*;

import java.util.List;

public interface EventServicePublic {
    List<EventShortDto> findEventsPublic(EventPublicRequestParams params, String clientIp, String endpoint);

    EventFullDto findEventByIdPublic(Long eventId, String clientIp, String endpoint);
}
