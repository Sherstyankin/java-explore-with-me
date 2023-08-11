package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.HitDto;
import ru.practicum.entity.Hit;

@UtilityClass
public class StatMapper {
    //private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Hit mapToHit(HitDto dto) {
        return Hit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }
}
