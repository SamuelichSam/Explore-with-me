package ru.practicum.mapper;

import ru.practicum.HitDto;
import ru.practicum.model.Hit;

public class StatMapper {
    public static Hit toHit(HitDto hitDto) {
        var hit = new Hit();
        hit.setApp(hitDto.app());
        hit.setUri(hitDto.uri());
        hit.setIp(hitDto.ip());
        hit.setTimestamp(hitDto.timestamp());
        return hit;
    }

    public static HitDto toDto(Hit hit) {
        return new HitDto(
                hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                hit.getTimestamp()
        );
    }
}
