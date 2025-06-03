package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitDto;
import ru.practicum.StatDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.StatMapper;
import ru.practicum.projection.StatProjection;
import ru.practicum.repo.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatRepository statRepository;

    @Transactional
    @Override
    public void createHit(HitDto hitDto) {
        log.info("Сохранение статистики - {}", hitDto);
        statRepository.save(StatMapper.toHit(hitDto));
    }

    @Override
    public List<StatDto> findStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }
        List<StatProjection> projections;
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                log.info("Получение статистики - uris empty, unique");
                projections = statRepository.findUniqueStatsByPeriod(start, end);
            } else {
                log.info("Получение статистики - uris empty, not unique");
                projections = statRepository.findStatsByPeriod(start, end);
            }
        } else {
            if (unique) {
                log.info("Получение статистики - uris not empty, unique");
                projections = statRepository.findUniqueStatsByPeriodAndUris(start, end, uris);
            } else {
                log.info("Получение статистики - uris not empty, not unique");
                projections = statRepository.findStatsByPeriodAndUris(start, end, uris);
            }
        }
        return projections.stream()
                .map(p -> new StatDto(p.getApp(), p.getUri(), p.getHits()))
                .toList();
    }
}
