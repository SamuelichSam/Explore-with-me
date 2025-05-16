package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitDto;
import ru.practicum.StatDto;
import ru.practicum.mapper.StatMapper;
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
        List<Object[]> statsData;
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                log.info("Получение статистики - uris empty, unique");
                statsData = statRepository.findUniqueStatsByPeriod(start, end);
            } else {
                log.info("Получение статистики - uris empty, not unique");
                statsData = statRepository.findStatsByPeriod(start, end);
            }
        } else {
            if (unique) {
                log.info("Получение статистики - uris not empty, unique");
                statsData = statRepository.findUniqueStatsByPeriodAndUris(start, end, uris);
            } else {
                log.info("Получение статистики - uris not empty, not unique");
                statsData = statRepository.findStatsByPeriodAndUris(start, end, uris);
            }
        }
        return statsData.stream()
                .map(row -> new StatDto(
                        (String) row[0],
                        (String) row[1],
                        (Long) row[2]
                ))
                .toList();
    }
}
