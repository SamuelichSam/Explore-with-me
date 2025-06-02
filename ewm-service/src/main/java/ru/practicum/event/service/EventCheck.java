package ru.practicum.event.service;

import lombok.experimental.UtilityClass;
import ru.practicum.event.model.State;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.Status;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class EventCheck {
    public void stateValidation(State eventState) {
        if (eventState == State.PUBLISHED) {
            throw new ConflictException("Изменить можно только отмененные события " +
                    "или события в состоянии ожидания модерации");
        }
    }

    public void dateCheck(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата события должна быть не раньше чем через два часа от текущего момента");
        }
    }

    public void datePeriodCheck(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }
    }

    public void dateAfterCheck(State eventState, LocalDateTime eventDate,
                               LocalDateTime publishedOn) {
        if (eventState == State.PUBLISHED &&
                eventDate.isBefore(publishedOn.plusHours(1))) {
            throw new ConflictException("Дата события должна быть не ранее чем через час после публикации");
        }
    }

    public void requestLimitCheck(Integer partLimit, Long confReq, Status status) {
        if (Status.CONFIRMED.equals(status) && confReq >= partLimit) {
            throw new ConflictException("Достигнут лимит одобренных заявок");
        }
    }

    public void requestStatusCheck(List<Request> requests) {
        requests.forEach(request -> {
            if (request.getStatus() != Status.PENDING) {
                throw new ConflictException("Заявка не в статусе ожидания");
            }
        });
    }
}
