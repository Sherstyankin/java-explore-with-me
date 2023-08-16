package ru.practicum.event.service;

import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.NewEventDto;
import ru.practicum.dto.request.UpdateEventAdminRequest;
import ru.practicum.dto.request.UpdateEventUserRequest;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortType;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventFullDto> getEvents(List<Long> users,
                                 List<EventState> states,
                                 List<Long> categories,
                                 LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd,
                                 Integer from,
                                 Integer size);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest dto);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto addNewEvent(Long userId, NewEventDto dto);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto);

    List<ParticipationRequestDto> getRequestsForUserEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatusForUserEvent(Long userId,
                                                                   Long eventId,
                                                                   EventRequestStatusUpdateRequest dto);

    List<EventShortDto> getEvents(String text,
                                  List<Long> categories,
                                  Boolean paid,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Boolean onlyAvailable,
                                  SortType sort,
                                  Integer from,
                                  Integer size,
                                  String ip);

    EventFullDto getEventById(Long id, String ip);
}
