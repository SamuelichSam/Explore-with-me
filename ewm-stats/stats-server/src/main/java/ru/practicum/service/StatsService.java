package ru.practicum.service;

import ru.practicum.HitDto;
import ru.practicum.StatDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void createHit(HitDto hitDto);

    List<StatDto> findStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
