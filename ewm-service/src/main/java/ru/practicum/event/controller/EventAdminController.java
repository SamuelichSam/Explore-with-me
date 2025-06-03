package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventServiceAdmin;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/events")
public class EventAdminController {
    private final EventServiceAdmin eventService;

    @GetMapping
    public List<EventFullDto> findEventsAdmin(@RequestParam(required = false) List<Long> users,
                                              @RequestParam(required = false) List<String> states,
                                              @RequestParam(required = false) List<Long> categories,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                              @RequestParam(defaultValue = "0") Integer from,
                                              @RequestParam(defaultValue = "10") Integer size) {
        var params = EventAdminRequestParams.of(users, states, categories, rangeStart, rangeEnd, from, size);

        return eventService.findEventsAdmin(params);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAdmin(@PathVariable Long eventId,
                                         @Valid @RequestBody UpdateEventAdminRequest dto) {
        return eventService.updateEventAdmin(eventId, dto);
    }
}
