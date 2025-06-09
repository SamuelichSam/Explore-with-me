package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitDto;
import ru.practicum.StatDto;
import ru.practicum.StatsClient;
import ru.practicum.category.model.Category;
import ru.practicum.category.repo.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.model.State;
import ru.practicum.event.repo.EventRepository;
import ru.practicum.event.repo.LocationRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.rating.model.RatingType;
import ru.practicum.rating.repo.RatingRepository;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.Status;
import ru.practicum.request.repo.RequestRepository;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventServiceImpl implements EventServiceAdmin, EventServicePrivate, EventServicePublic {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final RatingRepository ratingRepository;
    private final EventMapper eventMapper;
    private final RequestMapper requestMapper;
    private final UserMapper userMapper;
    private final StatsClient statsClient;

    @Override
    public List<EventShortDto> findEventsPrivate(Long userId, Integer from, Integer size) {
        log.info("Получение событий, добавленных пользователем с id - {}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id %s не найден".formatted(userId)));
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable).getContent();
        return events.stream()
                .map(eventMapper::toShortDto)
                .toList();
    }

    @Override
    public EventFullDto createEventPrivate(Long userId, NewEventDto dto) {
        log.info("Добавление нового события пользователем с id- {}", userId);
        EventCheck.dateCheck(LocalDateTime.parse(dto.eventDate(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id %s не найден".formatted(userId)));
        Category category = categoryRepository.findById(dto.category())
                .orElseThrow(() -> new NotFoundException("Категория с id %s не найдена".formatted(dto.category())));
        locationRepository.save(dto.location());
        Event event = eventMapper.toEvent(dto, requester, category);
        event.setCreatedOn(LocalDateTime.now());
        event.setConfirmedRequests(0L);
        event.setState(State.PENDING);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toFullDto(savedEvent);
    }

    @Override
    public EventFullDto findEventByIdPrivate(Long userId, Long eventId) {
        log.info("Получение полной информации о событии с id - {} добавленном пользователем с id - {}",
                eventId, userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id %s не найдено".formatted(eventId)));
        return eventMapper.toFullDto(event);
    }

    @Override
    public EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest dto) {
        log.info("Обновление события с id - {} добавленного пользователем с id - {}", eventId, userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id %s не найдено".formatted(eventId)));
        EventCheck.stateValidation(event.getState());
        EventCheck.dateCheck(event.getEventDate());
        EventUpdate.updateEventRequest(event, dto);
        if (dto.category() != null) {
            Category category = categoryRepository.findById(dto.category())
                    .orElseThrow(() -> new NotFoundException("Категория с id %s не найдена".formatted(dto.category())));
            event.setCategory(category);
        }
        if (dto.stateAction() != null) {
            switch (dto.stateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
                default -> throw new IllegalArgumentException("Неизвестное действие %s ".formatted(dto.stateAction()));
            }
        }
        return eventMapper.toFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> findEventRequestsPrivate(Long userId, Long eventId) {
        log.info("Получение информации о запросах на участие в событии с id - {} пользователя с id - {}",
                eventId, userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id %s не найден".formatted(userId)));
        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id %s не найдено".formatted(eventId)));
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequestsPrivate(Long userId, Long eventId,
                                                                     EventRequestStatusUpdateRequest dto) {
        log.info("Обновление статуса заявок на участие в событии с id - {} пользователя с id - {}", eventId, userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id %s не найдено".formatted(eventId)));
        Status status = Status.valueOf(dto.status().toUpperCase());
        if (event.getParticipantLimit() != 0 && event.getRequestModeration()) {
            EventCheck.requestLimitCheck(event.getParticipantLimit(), event.getConfirmedRequests(), status);
        }
        List<Request> requests = requestRepository.findAllById(dto.requestIds());
        EventCheck.requestStatusCheck(requests);
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();
        for (Request request : requests) {
            if (status == Status.CONFIRMED && event.getConfirmedRequests() >= event.getParticipantLimit()) {
                request.setStatus(Status.REJECTED);
                rejected.add(requestMapper.toDto(request));
            } else {
                request.setStatus(status);
                if (status == Status.CONFIRMED) {
                    confirmed.add(requestMapper.toDto(request));
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                } else {
                    rejected.add(requestMapper.toDto(request));
                }
            }
        }
        requestRepository.saveAll(requests);
        eventRepository.save(event);
        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

    @Override
    public EventFullDto findEventRatingPrivate(Long userId, Long eventId) {
        log.info("Получение рейтинга события с id - {} пользователя с id - {}", eventId, userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id %s не найден".formatted(userId)));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id %s не найдено".formatted(eventId)));
        Long likes = ratingRepository.countByEventIdAndRatingType(eventId, RatingType.LIKE);
        Long dislikes = ratingRepository.countByEventIdAndRatingType(eventId, RatingType.DISLIKE);
        int rating;
        Long score = likes - dislikes;
        if (likes + dislikes == 0) {
            rating = 5;
        } else {
            rating = (int) Math.min(10, Math.max(1, 5 + score / 2));
        }
        event.setRating(rating);
        return eventMapper.toFullDto(event);
    }

    @Override
    public List<EventFullDto> findEventsAdmin(EventAdminRequestParams params) {
        log.info("Поиск событий");
        Specification<Event> spec = EventSpec.specAdmin(params.users(), params.states(), params.categories(),
                params.rangeStart(), params.rangeEnd());
        Pageable pageable = PageRequest.of(params.from() / params.size(), params.size());
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        return events.stream()
                .map(eventMapper::toFullDto)
                .toList();
    }

    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest dto) {
        log.info("Обновление события с id - {} и его статуса", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id %s не найдено".formatted(eventId)));
        EventCheck.dateAfterCheck(event.getState(), event.getEventDate(),
                event.getPublishedOn());
        if (dto.stateAction() != null) {
            switch (dto.stateAction()) {
                case PUBLISH_EVENT -> {
                    if (event.getState() != State.PENDING) {
                        throw new ConflictException("Событие можно публиковать только если оно в состоянии ожидания");
                    }
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> {
                    if (event.getState() == State.PUBLISHED) {
                        throw new ConflictException("Нельзя отклонить опубликованное событие");
                    }
                    event.setState(State.CANCELED);
                }
            }
        }
        EventUpdate.updateEventRequestAdmin(event, dto, locationRepository);
        if (dto.category() != null) {
            Category category = categoryRepository.findById(dto.category())
                    .orElseThrow(() -> new NotFoundException("Категория с id %s не найдена".formatted(dto.category())));
            event.setCategory(category);
        }
        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toFullDto(updatedEvent);
    }

    @Override
    public List<EventShortDto> findEventsPublic(EventPublicRequestParams params, String clientIp, String endpoint) {
        log.info("Получение событий с возможностью фильтрации");
        Specification<Event> spec = EventSpec.specPublic(params.text(), params.categories(), params.paid(),
                params.rangeStart(), params.rangeEnd(), params.onlyAvailable());
        EventCheck.datePeriodCheck(params.rangeStart(), params.rangeEnd());
        EventSort eventSort = params.sort() != null ? EventSort.valueOf(params.sort().toUpperCase()) : null;
        Sort sorting = Sort.unsorted();
        if (eventSort != null) {
            sorting = eventSort.getSort();
            }
        Pageable pageable = PageRequest.of(params.from() / params.size(), params.size(), sorting);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        logHit(clientIp, endpoint);
        return events.stream()
                .map(eventMapper::toShortDto)
                .toList();
    }

    @Override
    public EventFullDto findEventByIdPublic(Long eventId, String clientIp, String endpoint) {
        log.info("Получение подробной информации об опубликованном событии с id - {}", eventId);
        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id %s не найдено".formatted(eventId)));
        logHit(clientIp, endpoint);
        List<StatDto> stats = statsClient.findStats(event.getPublishedOn(), LocalDateTime.now(),
                List.of("/events/" + eventId), true);
        Long views = stats.isEmpty() ? 0L : stats.getFirst().hits();
        event.setViews(views);
        return eventMapper.toFullDto(event);
    }

    @Override
    public List<EventShortDto> findTopEventsPublic(Integer count) {
        log.info("Получение топ {} событий по рейтингу", count);
        List<Event> events = eventRepository.findTopEvents(count);
        return events.stream()
                .map(eventMapper::toShortDto)
                .toList();
    }

    @Override
    public List<UserShortDto> findTopAuthorsPublic(Integer count) {
        log.info("Получение топ {} авторов по рейтингу", count);
        List<User> users = userRepository.findTopUsers(count);
        return users.stream()
                .map(userMapper::toShortDto)
                .toList();
    }

    private void logHit(String clientIp, String endpoint) {
        log.info("Отправка информации о просмотре в сервис статистики");
        HitDto hitDto = new HitDto(
                "explore-with-me",
                endpoint,
                clientIp,
                LocalDateTime.now()
        );
        statsClient.createHit(hitDto);
    }
}
