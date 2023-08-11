package ru.practicum.request.service;

import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.entity.Request;

import java.util.List;
import java.util.Map;

public interface EventRequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto addRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    Map<Long, Long> getConfirmedRequests(List<Long> eventIds);

    Long getConfirmedRequestsByEventId(Long eventId);

    List<Request> getRequestsForUserEvent(Long eventId);

    List<Request> getAllRequestByIds(List<Long> requestIds);

    void updateRequests(List<Request> requestsToUpdate);
}
