package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.entity.Hit;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.StatMapper;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class StatServiceImpl implements StatService {

    private final StatRepository statRepository;

    @Override
    @Transactional
    public void saveHit(HitDto hitDto) {
        Hit hit = StatMapper.mapToHit(hitDto);
        statRepository.save(hit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStatistics(LocalDateTime start,
                                            LocalDateTime end,
                                            List<String> uris,
                                            boolean unique) {
        checkTime(start, end);
        if (uris != null && !uris.isEmpty() && unique) {
            log.info("Получить статистику уникальных обращений, согласно списку URI: {}", uris);
            return statRepository.getDistinctStatisticsOfUris(start, end, uris);
        } else if (uris != null && !uris.isEmpty()) {
            log.info("Получить статистику всех обращений, согласно списку URI: {}", uris);
            return statRepository.getStatisticsOfUris(start, end, uris);
        } else if (unique) {
            log.info("Получить статистику всех уникальных обращений");
            return statRepository.getDistinctStatistics(start, end);
        } else {
            log.info("Получить статистику всех обращений");
            return statRepository.getStatistics(start, end);
        }
    }

    private void checkTime(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end) || start.equals(end)) {
            log.error("Время окончания: {} не может быть раньше начала: {}", end, start);
            throw new ValidationException("Время окончания: " + end + " не может быть раньше начала: " +
                    start);
        }
    }
}
