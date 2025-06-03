package ru.practicum.event.service;

import ru.practicum.event.dto.*;

import java.util.List;

public interface EventServiceAdmin {
    List<EventFullDto> findEventsAdmin(EventAdminRequestParams params);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest dto);
}
