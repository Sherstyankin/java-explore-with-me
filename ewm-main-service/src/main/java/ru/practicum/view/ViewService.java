package ru.practicum.view;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface ViewService {
    Map<Long, Long> getViews(List<Long> eventIds);

    Long getViewsById(Long eventId);

    ResponseEntity<Object> saveHit(String uri, String ip);
}
