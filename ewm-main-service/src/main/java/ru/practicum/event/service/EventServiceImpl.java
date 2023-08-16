package ru.practicum.event.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.service.CategoryService;
import ru.practicum.comment.service.CommentService;
import ru.practicum.dto.*;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.NewEventDto;
import ru.practicum.dto.request.UpdateEventAdminRequest;
import ru.practicum.dto.request.UpdateEventUserRequest;
import ru.practicum.entity.*;
import ru.practicum.enums.*;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.location.LocationRepository;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.request.service.EventRequestService;
import ru.practicum.user.service.UserService;
import ru.practicum.view.ViewService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.querydsl.core.types.ExpressionUtils.count;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final CommentService commentService;
    private final EventRepository eventRepository;
    private final ViewService viewService;
    private final EventRequestService eventRequestService;
    private final LocationRepository locationRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEvents(List<Long> initiators,
                                        List<EventState> states,
                                        List<Long> categories,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        Integer from,
                                        Integer size) {
        Pageable page = PageRequest.of(from, size, Sort.by("id"));
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(byInitiators(initiators))
                .and(byStates(states))
                .and(byCategories(categories))
                .and(byRangeStartAndRangeEnd(rangeStart, rangeEnd));
        List<Event> events = eventRepository.findAll(predicate, page).getContent();
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> views = viewService.getViews(eventIds);
        Map<Long, Long> confirmedRequests = eventRequestService.getConfirmedRequests(eventIds);
        Map<Long, List<Comment>> comments = commentService.getAndMapCommentsByEventIds(eventIds);
        return EventMapper.mapToEventFullDto(events, views, confirmedRequests, comments);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest dto) {
        Event event = findEventById(eventId);
        if (dto.getEventDate() != null) {
            checkTime(dto.getEventDate(), 1);
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getStateAction() != null) {
            updateEventStateByAdmin(event, dto);
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            event.setCategory(categoryService.findCategoryById(dto.getCategory()));
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getLocation() != null) {
            Location location = Location.builder()
                    .lon(dto.getLocation().getLon())
                    .lat(dto.getLocation().getLat())
                    .build();
            locationRepository.save(location);
            event.setLocation(location);
        }
        if (dto.isPaid()) {
            event.setPaid(dto.isPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.isRequestModeration()) {
            event.setRequestModeration(dto.isRequestModeration());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        Long views = viewService.getViewsById(eventId);
        Long confirmedRequests = eventRequestService.getConfirmedRequestsByEventId(eventId);
        List<CommentDto> commentDtos = CommentMapper
                .mapToCommentDto(commentService.getCommentsByEventId(eventId));
        return EventMapper.mapToEventFullDto(eventRepository.save(event),
                views,
                confirmedRequests,
                commentDtos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        Pageable page = PageRequest.of(from, size, Sort.by("id"));
        List<Event> events = eventRepository.findByInitiatorId(userId, page);
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> views = viewService.getViews(eventIds);
        Map<Long, Long> confirmedRequests = eventRequestService.getConfirmedRequests(eventIds);
        return EventMapper.mapToEventShortDto(events, views, confirmedRequests);
    }

    @Override
    public EventFullDto addNewEvent(Long userId, NewEventDto dto) {
        checkTime(dto.getEventDate(), 2);
        Category category = categoryService.findCategoryById(dto.getCategory());
        User user = userService.getUserById(userId);
        Event event = EventMapper.mapToEvent(dto, category, user);
        locationRepository.save(event.getLocation());
        Event eventResponse = eventRepository.save(event);
        return EventMapper.mapToEventFullDto(eventResponse, 0L, 0L, null);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Optional<Event> event = Optional.ofNullable(eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Событие по ID: " + eventId + " не найдено.")));
        Long views = viewService.getViewsById(eventId);
        Long confirmedRequests = eventRequestService.getConfirmedRequestsByEventId(eventId);
        List<CommentDto> commentDtos = CommentMapper
                .mapToCommentDto(commentService.getCommentsByEventId(eventId));
        return EventMapper.mapToEventFullDto(event.get(), views, confirmedRequests, commentDtos);
    }

    @Override
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        userService.getUserById(userId);
        Event event = findEventById(eventId);
        EventState state = event.getState();
        if (state == EventState.PUBLISHED) {
            log.error("Изменение события невозможно, так как оно уже опубликовано: {}", state);
            throw new ConflictException("Изменение события невозможно, " +
                    "так как оно уже опубликовано:" + state);
        }
        if (dto.getEventDate() != null) {
            checkTime(dto.getEventDate(), 2);
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getStateAction() != null) {
            updateEventStateByUser(event, dto);
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            event.setCategory(categoryService.findCategoryById(dto.getCategory()));
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getLocation() != null) {
            Location location = Location.builder()
                    .lon(dto.getLocation().getLon())
                    .lat(dto.getLocation().getLat())
                    .build();
            locationRepository.save(location);
            event.setLocation(location);
        }
        if (dto.isPaid()) {
            event.setPaid(dto.isPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.isRequestModeration()) {
            event.setRequestModeration(dto.isRequestModeration());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        Long views = viewService.getViewsById(eventId);
        Long confirmedRequests = eventRequestService.getConfirmedRequestsByEventId(eventId);
        List<CommentDto> commentDtos = CommentMapper
                .mapToCommentDto(commentService.getCommentsByEventId(eventId));
        return EventMapper.mapToEventFullDto(eventRepository.save(event),
                views,
                confirmedRequests,
                commentDtos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsForUserEvent(Long userId, Long eventId) {
        getUserEventById(userId, eventId);
        List<Request> requests = eventRequestService.getRequestsForUserEvent(eventId);
        return RequestMapper.mapToParticipationRequestDto(requests);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatusForUserEvent(Long userId,
                                                                          Long eventId,
                                                                          EventRequestStatusUpdateRequest dto) {
        userService.getUserById(userId);
        Event event = findEventById(eventId);
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            log.error("Модерация заявки не требуется.");
            throw new ConflictException("Модерация заявки не требуется.");
        }
        if (dto.getStatus().equals(ParticipationStatus.PENDING)) {
            log.error("Обновляемый статус не может быть: {}", dto.getStatus());
            throw new ConflictException("Обновляемый статус не может быть: " + dto.getStatus());
        }
        Long confirmedRequest = eventRequestService.getConfirmedRequestsByEventId(eventId);
        if (confirmedRequest >= event.getParticipantLimit()) {
            log.error("Предел участников достигнут.");
            throw new ConflictException("Предел участников достигнут.");
        }
        List<Long> requestIds = dto.getRequestIds();
        List<Request> requestsToUpdate = eventRequestService.getAllRequestByIds(requestIds);
        long vacantPlaces = event.getParticipantLimit() - confirmedRequest;
        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();
        for (Request request : requestsToUpdate) {
            if (!request.getStatus().equals(ParticipationStatus.PENDING)) {
                log.error("Статус не может быть изменен, если заявка не в статусе ожидания модерации.");
                throw new ConflictException("Статус не может быть изменен, " +
                        "если заявка не в статусе ожидания модерации.");
            }
            if (vacantPlaces > 0 && dto.getStatus().equals(ParticipationStatus.CONFIRMED)) {
                request.setStatus(ParticipationStatus.CONFIRMED);
                confirmedRequests.add(request);
                vacantPlaces--;
            } else {
                request.setStatus(ParticipationStatus.REJECTED);
                rejectedRequests.add(request);
            }
        }
        eventRequestService.updateRequests(requestsToUpdate);
        return EventMapper.mapToEventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(String text,
                                         List<Long> categories,
                                         Boolean paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Boolean onlyAvailable,
                                         SortType sort,
                                         Integer from,
                                         Integer size,
                                         String ip) {

        Pageable page = PageRequest.of(from, size, Sort.by("id"));
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(byPublishedState())
                .and(byText(text))
                .and(byCategories(categories))
                .and(byPaid(paid))
                .and(byRangeStartAndRangeEndOrLaterThanNow(rangeStart, rangeEnd))
                .and(byOnlyAvailable(onlyAvailable));
        List<Event> events = eventRepository.findAll(predicate, page).getContent();
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        viewService.saveHit("/events", ip);
        Map<Long, Long> views = viewService.getViews(eventIds);
        Map<Long, Long> confirmedRequests = eventRequestService.getConfirmedRequests(eventIds);
        List<EventShortDto> eventShortDtos = EventMapper.mapToEventShortDto(events, views, confirmedRequests);
        if (sort == SortType.EVENT_DATE) {
            eventShortDtos = eventShortDtos.stream().sorted(Comparator.comparing(EventShortDto::getEventDate))
                    .collect(Collectors.toList());
        } else {
            eventShortDtos = eventShortDtos.stream().sorted(Comparator.comparing(EventShortDto::getViews))
                    .collect(Collectors.toList());
        }
        return eventShortDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long id, String ip) {
        Event event = findEventById(id);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.error("Событие с ID:{} не опубликовано.", id);
            throw new EntityNotFoundException("Событие с ID:" + id + " не опубликовано.");
        }
        viewService.saveHit("/events/" + id, ip);
        Long views = viewService.getViewsById(id);
        Long confirmedRequests = eventRequestService.getConfirmedRequestsByEventId(id);
        List<CommentDto> commentDtos = CommentMapper
                .mapToCommentDto(commentService.getCommentsByEventId(id));
        return EventMapper.mapToEventFullDto(event, views, confirmedRequests, commentDtos);
    }

    @Override
    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие по ID: " + eventId + " не найдено."));
    }

    private BooleanExpression byInitiators(List<Long> initiators) {
        return initiators != null ? QEvent.event.initiator.id.in(initiators) : null;
    }

    private BooleanExpression byStates(List<EventState> states) {
        return states != null ? QEvent.event.state.in(states) : null;
    }

    private BooleanExpression byPublishedState() {
        return QEvent.event.state.eq(EventState.PUBLISHED);
    }

    private BooleanExpression byText(String text) {
        return text != null && !text.isBlank() ? QEvent.event.annotation.likeIgnoreCase("%" + text + "%")
                .or(QEvent.event.description.likeIgnoreCase("%" + text + "%")) : null;
    }

    private BooleanExpression byCategories(List<Long> categories) {
        return categories != null ? QEvent.event.category.id.in(categories) : null;
    }

    private BooleanExpression byPaid(Boolean isPaid) {
        return isPaid != null ? QEvent.event.paid.eq(isPaid) : null;
    }

    private BooleanExpression byOnlyAvailable(Boolean onlyAvailable) {
        return onlyAvailable ? QEvent.event.participantLimit.eq(0)
                .or(QEvent.event.participantLimit
                        .gt(JPAExpressions
                                .select(count(QRequest.request.id))
                                .from(QRequest.request)
                                .where(QRequest.request.status.eq(ParticipationStatus.CONFIRMED)
                                        .and(QRequest.request.event.id.eq(QEvent.event.id))))) : null;
    }

    private BooleanExpression byRangeStartAndRangeEnd(LocalDateTime rangeStart,
                                                      LocalDateTime rangeEnd) {
        return rangeStart != null && rangeEnd != null ?
                QEvent.event.eventDate.between(rangeStart, rangeEnd) : null;
    }

    private BooleanExpression byRangeStartAndRangeEndOrLaterThanNow(LocalDateTime rangeStart,
                                                                    LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            checkTime(rangeStart, rangeEnd);
            return QEvent.event.eventDate.between(rangeStart, rangeEnd);
        } else {
            return QEvent.event.eventDate.gt(LocalDateTime.now());
        }
    }

    private void checkTime(LocalDateTime time, int hours) {
        if (time.isBefore(LocalDateTime.now().plusHours(hours))) {
            log.error("Дата начала изменяемого события" +
                    " должна быть не ранее чем за {} от даты публикации", hours);
            throw new ValidationException("Дата начала изменяемого события " +
                    "должна быть не ранее чем за " + hours + " от даты публикации");
        }
    }

    private void checkTime(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            log.error("Дата начала события: {}" +
                    " должна быть не позже даты окончания события: {}", start, end);
            throw new ValidationException("Дата начала события: " + start +
                    " должна быть не позже даты окончания события: " + end);
        }
    }


    private void updateEventStateByAdmin(Event event, UpdateEventAdminRequest dto) {
        EventState state = event.getState();
        AdminStateAction adminStateAction = dto.getStateAction();
        if (adminStateAction == AdminStateAction.PUBLISH_EVENT) {
            if (state != EventState.PENDING) {
                log.error("Публикация события невозможна, так как уже определен следующий статус: {}", state);
                throw new ConflictException("Публикация невозможна, так как уже " +
                        "определен следующий статус: " + state);
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }
        if (adminStateAction == AdminStateAction.REJECT_EVENT) {
            if (state == EventState.PUBLISHED) {
                log.error("Отмена события невозможна, так как уже определен следующий статус: {}", state);
                throw new ConflictException("Отмена невозможна, так как уже " +
                        "определен следующий статус: " + state);
            }
            event.setState(EventState.CANCELED);
        }
    }

    private void updateEventStateByUser(Event event, UpdateEventUserRequest dto) {
        UserStateAction userStateAction = dto.getStateAction();
        if (userStateAction == UserStateAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        } else {
            event.setState(EventState.CANCELED);
        }
    }
}
