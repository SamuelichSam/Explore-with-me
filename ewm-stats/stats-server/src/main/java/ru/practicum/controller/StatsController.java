package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.HitDto;
import ru.practicum.StatDto;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    public void createHit(@RequestBody HitDto dto) {
        statsService.createHit(dto);
    }

    @GetMapping("/stats")
    public List<StatDto> findStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime start,
                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime end,
                                   @RequestParam(required = false) List<String> uris,
                                   @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        return statsService.findStats(start, end, uris, unique);
    }
}
