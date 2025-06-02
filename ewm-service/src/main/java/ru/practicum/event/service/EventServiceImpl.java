package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
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
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.Status;
import ru.practicum.request.repo.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final RequestMapper requestMapper;
    private final StatsClient statsClient;

    @Override
    public List<EventShortDto> findEventsPrivate(Long userId, Integer from, Integer size) {
        log.info("Получение событий, добавленных пользователем с id - {}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
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
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Category category = categoryRepository.findById(dto.category())
                .orElseThrow(() -> new NotFoundException("Категория с id " + dto.category() + " не найдена"));
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
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId +
                        " не найдено для пользователя с id " + userId));
        return eventMapper.toFullDto(event);
    }

    @Transactional
    @Override
    public EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest dto) {
        log.info("Обновление события с id - {} добавленного пользователем с id - {}", eventId, userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
        EventCheck.stateValidation(event.getState());
        EventCheck.dateCheck(event.getEventDate());
        EventUpdate.updateEventRequest(event, dto, locationRepository);
        if (dto.category() != null) {
            Category category = categoryRepository.findById(dto.category())
                    .orElseThrow(() -> new NotFoundException("Категория с id " + dto.category() + " не найдена"));
            event.setCategory(category);
        }
        if (dto.stateAction() != null) {
            switch (dto.stateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
                default -> throw new IllegalArgumentException("Неизвестное действие " + dto.stateAction());
            }
        }

        return eventMapper.toFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> findEventRequestsPrivate(Long userId, Long eventId) {
        log.info("Получение информации о запросах на участие в событии с id - {} пользователя с id - {}",
                eventId, userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateEventRequestsPrivate(Long userId, Long eventId,
                                                                     EventRequestStatusUpdateRequest dto) {
        log.info("Обновление статуса заявок на участие в событии с id - {} пользователя с id - {}", eventId, userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
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
    public List<EventFullDto> findEventsAdmin(List<Long> users, List<String> states, List<Long> categories,
                                              LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from,
                                              Integer size) {
        log.info("Поиск событий");
        Specification<Event> spec = EventSpec.specAdmin(users, states, categories, rangeStart, rangeEnd);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        return events.stream()
                .map(eventMapper::toFullDto)
                .toList();
    }

    @Transactional
    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest dto) {
        log.info("Обновление события с id - {} и его статуса", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId +
                        " не найдено"));
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
        EventUpdate.updateEventRequest(event, dto, locationRepository);
        if (dto.category() != null) {
            Category category = categoryRepository.findById(dto.category())
                    .orElseThrow(() -> new NotFoundException("Категория с id " + dto.category() + " не найдена"));
            event.setCategory(category);
        }
        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toFullDto(updatedEvent);
    }

    @Override
    public List<EventShortDto> findEventsPublic(String text, List<Long> categories, Boolean paid,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                Boolean onlyAvailable, String sort, Integer from,
                                                Integer size, HttpServletRequest request) {
        log.info("Получение событий с возможностью фильтрации");
        Specification<Event> spec = EventSpec.specPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable);
        EventCheck.datePeriodCheck(rangeStart, rangeEnd);
        EventSort eventSort = sort != null ? EventSort.valueOf(sort.toUpperCase()) : null;
        Sort sorting = Sort.unsorted();
        if (eventSort != null) {
            if (eventSort == EventSort.EVENT_DATE) {
                sorting = Sort.by(Sort.Direction.DESC, "eventDate");
            } else if (eventSort == EventSort.VIEWS) {
                sorting = Sort.by(Sort.Direction.DESC, "views");
            }
        }
        Pageable pageable = PageRequest.of(from / size, size, sorting);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        logHit(request);
        return events.stream()
                .map(eventMapper::toShortDto)
                .toList();
    }

    @Override
    public EventFullDto findEventByIdPublic(Long eventId, HttpServletRequest request) {
        log.info("Получение подробной информации об опубликованном событии с id - {}", eventId);
        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId +
                        " не найдено"));
        logHit(request);
        List<StatDto> stats = statsClient.findStats(event.getPublishedOn(), LocalDateTime.now(),
                List.of("/events/" + eventId), true);
        Long views = stats.isEmpty() ? 0L : stats.getFirst().hits();
        event.setViews(views);
        return eventMapper.toFullDto(event);
    }

    private void logHit(HttpServletRequest request) {
        log.info("Отправка информации о просмотре в сервис статистики");
        HitDto hitDto = new HitDto(
                "explore-with-me",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        );
        statsClient.createHit(hitDto);
    }
}
