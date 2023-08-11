package ru.practicum.view;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitDto;
import ru.practicum.StatClient;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class ViewServiceImpl implements ViewService {

    private static final String APP = "ewm-main-service";

    private final StatClient statClient;

    @Override
    public ResponseEntity<Object> saveHit(String uri, String ip) {
        HitDto hit = HitDto.builder()
                .ip(ip)
                .app(APP)
                .uri(uri)
                .build();
        return statClient.saveHit(hit);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getViews(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> urisList = new ArrayList<>();
        for (Long id : eventIds) {
            urisList.add("/events/" + id);
        }
        List<ViewStatsDto> statsList = statClient.getStatistics(
                LocalDateTime.now().minusDays(60),
                LocalDateTime.now(),
                urisList,
                true);
        return statsList != null ? statsList.stream()
                .collect(Collectors.toMap(
                        viewStats -> Long.parseLong(viewStats.getUri().substring("/events/".length())),
                        ViewStatsDto::getHits
                )) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getViewsById(Long eventId) {
        List<String> urisList = new ArrayList<>();
        urisList.add("/events/" + eventId);
        List<ViewStatsDto> statsList = statClient.getStatistics(
                LocalDateTime.now().minusDays(60),
                LocalDateTime.now(),
                urisList,
                true);
        return !statsList.isEmpty() ? statsList.get(0).getHits() : 0L;
    }
}
