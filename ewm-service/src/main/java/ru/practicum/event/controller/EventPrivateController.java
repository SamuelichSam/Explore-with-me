package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventServicePrivate;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.service.RatingService;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}/events")
public class EventPrivateController {
    private final EventServicePrivate eventService;
    private final RatingService ratingService;

    @GetMapping
    public List<EventShortDto> findEventsPrivate(@PathVariable Long userId, @RequestParam(defaultValue = "0") Integer from,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        return eventService.findEventsPrivate(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEventPrivate(@PathVariable Long userId, @Valid @RequestBody NewEventDto dto) {
        return eventService.createEventPrivate(userId, dto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto findEventByIdPrivate(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.findEventByIdPrivate(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventPrivate(@PathVariable Long userId,
                                           @PathVariable Long eventId,
                                           @Valid @RequestBody UpdateEventUserRequest dto) {
        return eventService.updateEventPrivate(userId, eventId, dto);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> findEventRequestsPrivate(@PathVariable Long userId,
                                                                  @PathVariable Long eventId) {
        return eventService.findEventRequestsPrivate(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequestsPrivate(@PathVariable Long userId,
                                                                     @PathVariable Long eventId,
                                                                     @Valid @RequestBody EventRequestStatusUpdateRequest dto) {
        return eventService.updateEventRequestsPrivate(userId, eventId, dto);
    }

    @PostMapping("{eventId}/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public RatingDto rateEventPrivate(@PathVariable Long userId,
                                      @PathVariable Long eventId,
                                      @RequestParam Boolean liked) {
        return ratingService.rateEventPrivate(userId, eventId, liked);
    }

    @PatchMapping("{eventId}/ratings/{ratingId}")
    public RatingDto updateRatePrivate(@PathVariable Long userId,
                                       @PathVariable Long eventId,
                                       @PathVariable Long ratingId,
                                       @RequestParam Boolean liked) {
        return ratingService.updateRatePrivate(userId, eventId, ratingId, liked);
    }

    @GetMapping("{eventId}/ratings")
    public EventFullDto findEventRatingPrivate(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.findEventRatingPrivate(userId, eventId);
    }

    @DeleteMapping("{eventId}/ratings/{ratingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRatingPrivate(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @PathVariable Long ratingId) {
        ratingService.deleteRatingPrivate(userId, eventId, ratingId);
    }
}
