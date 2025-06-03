package ru.practicum.event.dto;

import java.util.List;

public record EventRequestStatusUpdateRequest(
        List<Long> requestIds,
        String status
) {
}
