package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventPublicRequestParams;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventServicePublic;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/events")
public class EventPublicController {
    private final EventServicePublic eventService;

    @GetMapping
    public List<EventShortDto> findEventsPublic(@RequestParam(required = false) String text,
                                                @RequestParam(required = false) List<Long> categories,
                                                @RequestParam(required = false) Boolean paid,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                @RequestParam(required = false) String sort,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size,
                                                HttpServletRequest request) {
        var params = EventPublicRequestParams.of(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort,
                from, size);
        String clientIp = request.getRemoteAddr();
        String endpoint = request.getRequestURI();
        return eventService.findEventsPublic(params, clientIp, endpoint);
    }

    @GetMapping("/{eventId}")
    public EventFullDto findEventByIdPublic(@PathVariable Long eventId, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String endpoint = request.getRequestURI();
        return eventService.findEventByIdPublic(eventId, clientIp, endpoint);
    }
}
