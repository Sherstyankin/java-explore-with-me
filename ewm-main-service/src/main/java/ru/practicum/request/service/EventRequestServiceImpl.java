package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.entity.Event;
import ru.practicum.entity.Request;
import ru.practicum.entity.User;
import ru.practicum.enums.EventState;
import ru.practicum.enums.ParticipationStatus;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.request.repository.EventRequestRepository;
import ru.practicum.user.service.UserService;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class EventRequestServiceImpl implements EventRequestService {

    private final EventRequestRepository eventRequestRepository;
    private final UserService userService;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userService.getUserById(userId);
        List<Request> requests = eventRequestRepository.findByRequesterId(userId);
        return RequestMapper.mapToParticipationRequestDto(requests);
    }

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User user = userService.getUserById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие по ID: " + eventId + " не найдено."));
        long confirmedRequestsScore = getConfirmedRequestsByEventId(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            log.error("Невозможно участвовать в неопубликованном событии");
            throw new ConflictException("Невозможно участвовать в неопубликованном событии");
        }
        if (event.getInitiator().getId().equals(userId)) {
            log.error("Инициатор события не может подать заявку на участие");
            throw new ConflictException("Инициатор события не может подать заявку на участие");
        }
        if (event.getParticipantLimit() != 0 && confirmedRequestsScore >= event.getParticipantLimit()) {
            log.error("Лимит на участие достигнут");
            throw new ConflictException("Лимит на участие достигнут");
        }
        Request request = Request.builder()
                .requester(user)
                .event(event)
                .build();
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(ParticipationStatus.CONFIRMED);
        } else {
            request.setStatus(ParticipationStatus.PENDING);
        }
        return RequestMapper.mapToParticipationRequestDto(eventRequestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        userService.getUserById(userId);
        Request request = getRequestById(requestId);
        request.setStatus(ParticipationStatus.CANCELED);
        return RequestMapper.mapToParticipationRequestDto(eventRequestRepository.save(request));
    }

    @Override
    public Map<Long, Long> getConfirmedRequests(List<Long> eventIds) {
        return eventRequestRepository
                .findByEventIdsAndConfirmedStatus(eventIds).stream()
                .collect(groupingBy(request -> request.getEvent().getId(), counting()));
    }

    @Override
    public Long getConfirmedRequestsByEventId(Long eventId) {
        return eventRequestRepository.findCountedRequestsByEventIdAndConfirmedStatus(eventId);
    }

    @Override
    public List<Request> getRequestsForUserEvent(Long eventId) {
        return eventRequestRepository.findByEventId(eventId);
    }

    @Override
    public List<Request> getAllRequestByIds(List<Long> requestIds) {
        return eventRequestRepository.findAllById(requestIds);
    }

    @Override
    public void updateRequests(List<Request> requestsToUpdate) {
        eventRequestRepository.saveAll(requestsToUpdate);
    }

    private Request getRequestById(Long id) {
        return eventRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Запрос по ID: " + id + " не найден."));
    }
}
