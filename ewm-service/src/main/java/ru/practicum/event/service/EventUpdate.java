package ru.practicum.event.service;

import lombok.experimental.UtilityClass;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.dto.UpdateEventRequest;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.repo.LocationRepository;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class EventUpdate {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void updateEventRequest(Event event, UpdateEventRequest updateRequest) {
        if (updateRequest.annotation() != null) {
            event.setAnnotation(updateRequest.annotation());
        }

        if (updateRequest.description() != null) {
            event.setDescription(updateRequest.description());
        }

        if (updateRequest.eventDate() != null) {
            if (LocalDateTime.parse(updateRequest.eventDate(), formatter).isBefore(LocalDateTime.now())) {
                throw new ValidationException("Эта дата уже наступила");
            }
            event.setEventDate(LocalDateTime.parse(updateRequest.eventDate(), formatter));
        }

        if (updateRequest.paid() != null) {
            event.setPaid(updateRequest.paid());
        }

        if (updateRequest.participantLimit() != null) {
            event.setParticipantLimit(updateRequest.participantLimit());
        }

        if (updateRequest.requestModeration() != null) {
            event.setRequestModeration(updateRequest.requestModeration());
        }

        if (updateRequest.title() != null) {
            event.setTitle(updateRequest.title());
        }
    }

    public void updateEventRequestAdmin(Event event, UpdateEventAdminRequest adminRequest, LocationRepository locationRepo) {
        updateEventRequest(event, adminRequest);
        if (adminRequest.location() != null) {
            Location location = adminRequest.location();
            if (location.getId() == null) {
                location = locationRepo.save(location);
            } else {
                Location finalLocation = location;
                location = locationRepo.findById(location.getId())
                        .orElseGet(() -> locationRepo.save(finalLocation));
            }
            event.setLocation(location);
        }
    }
}
