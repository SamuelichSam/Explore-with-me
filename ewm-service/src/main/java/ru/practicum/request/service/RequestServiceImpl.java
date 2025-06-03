package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.repo.EventRepository;
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
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> findUserRequests(Long userId) {
        log.info("Получение информации о заявках пользователя с id - {}", userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createUserRequest(Long userId, Long eventId) {
        log.info("Добавление запроса от пользователя с id - {}", userId);
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
        }
        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Запрос от пользователя с id " + userId + " на участие в событии с id "
                    + eventId + " уже есть");
        }
        if (event.getParticipantLimit() != 0 && requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED)
        >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит запросов на участие в событии с id " + eventId);
        }
        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(requester);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(Status.CONFIRMED);
        } else {
            request.setStatus(Status.PENDING);
        }
        Request savedRequest = requestRepository.save(request);
        return requestMapper.toDto(savedRequest);
    }

    @Override
    public ParticipationRequestDto cancelUserRequest(Long userId, Long requestId) {
        log.info("Отмена запроса с id - {} пользователем с id - {}", requestId, userId);
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + userId + " не найден"));
        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Запрос с id " + requestId + " не принадлежит пользователю с id " + userId);
        }
        if (request.getStatus() == Status.CANCELED) {
            throw new ConflictException("Запрос уже отменен");
        }
        request.setStatus(Status.CANCELED);
        Request canceledRequest = requestRepository.save(request);
        return requestMapper.toDto(canceledRequest);
    }
}
