package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.request.service.EventRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
@Slf4j
public class PrivateEventRequestController {

    private final EventRequestService eventRequestService;

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        log.info("Получить список запросов на участие пользователя c ID:{}", userId);
        return eventRequestService.getUserRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable Long userId,
                                              @RequestParam Long eventId) {
        log.info("Создать запрос на участие в событии с ID:{} для пользователя с ID:{}", eventId, userId);
        ParticipationRequestDto dto = eventRequestService.addRequest(userId, eventId);
        log.info("Создан запрос на участие в событии с ID:{} для пользователя с ID:{}", eventId, userId);
        return dto;
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        log.info("Отменить запрос на участие c ID:{} пользователя c ID:{}", requestId, userId);
        return eventRequestService.cancelRequest(userId, requestId);
    }
}
