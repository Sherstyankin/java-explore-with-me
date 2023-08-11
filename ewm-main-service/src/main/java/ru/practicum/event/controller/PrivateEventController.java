package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.NewEventDto;
import ru.practicum.dto.request.UpdateEventUserRequest;
import ru.practicum.event.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                             @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Получить события пользователя с ID:{}", userId);
        List<EventShortDto> eventShortDtos = eventService.getUserEvents(userId, from, size);
        log.info("Получен список событий пользователя с ID:{}", userId);
        return eventShortDtos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addNewEvent(@PathVariable Long userId,
                                    @RequestBody @Valid NewEventDto dto) {
        log.info("Добавить следующее событие: {}", dto);
        EventFullDto responseDto = eventService.addNewEvent(userId, dto);
        log.info("Cобытие добавлено: {}", dto);
        return responseDto;
    }

    @GetMapping("/{eventId}")
    public EventFullDto getUserEventById(@PathVariable Long userId,
                                         @PathVariable Long eventId) {
        log.info("Получить событие по ID: {}, инициатора с ID: {}", eventId, userId);
        return eventService.getUserEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateUserEvent(@PathVariable Long userId,
                                        @PathVariable Long eventId,
                                        @RequestBody @Valid UpdateEventUserRequest dto) {
        log.info("Изменить событие по ID: {}, инициатора с ID: {}", eventId, userId);
        return eventService.updateUserEvent(userId, eventId, dto);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsForUserEvent(@PathVariable Long userId,
                                                                 @PathVariable Long eventId) {
        log.info("Получить список запросов на участие в событии c ID: {}, инициатора с ID: {}", eventId, userId);
        return eventService.getRequestsForUserEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestsStatusForUserEvent(@PathVariable Long userId,
                                                                           @PathVariable Long eventId,
                                                                           @RequestBody EventRequestStatusUpdateRequest dto) {
        log.info("Обновить статус запроса на участие в событии по ID: {}, " +
                "инициатора с ID: {}", eventId, userId);
        return eventService.updateRequestStatusForUserEvent(userId, eventId, dto);
    }
}
