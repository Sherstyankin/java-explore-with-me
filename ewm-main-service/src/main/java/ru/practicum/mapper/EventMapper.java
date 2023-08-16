package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.request.NewEventDto;
import ru.practicum.entity.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@UtilityClass
public class EventMapper {

    public Event mapToEvent(NewEventDto dto,
                            Category category,
                            User initiator) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(category)
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .initiator(initiator)
                .location(LocationMapper.mapToLocation(dto.getLocation()))
                .paid(dto.isPaid())
                .participantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0)
                .requestModeration(dto.isRequestModeration())
                .title(dto.getTitle())
                .build();
    }

    public EventFullDto mapToEventFullDto(Event event,
                                          Long views,
                                          Long confirmedRequests,
                                          List<CommentDto> eventCommentDtos) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.mapToCategoryDto(event.getCategory()))
                .createdOn(event.getCreatedOn())
                .state(event.getState())
                .confirmedRequests(confirmedRequests)
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.mapToUserShortDto(event.getInitiator()))
                .location(LocationMapper.mapToLocationDto(event.getLocation()))
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn() != null ? event.getPublishedOn() : null)
                .requestModeration(event.isRequestModeration())
                .title(event.getTitle())
                .views(views)
                .comments(eventCommentDtos)
                .build();
    }

    public EventShortDto mapToEventShortDto(Event event, Long views, Long confirmedRequests) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.mapToCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate())
                .initiator(UserMapper.mapToUserShortDto(event.getInitiator()))
                .paid(event.isPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public EventRequestStatusUpdateResult mapToEventRequestStatusUpdateResult(List<Request> confirmedRequests,
                                                                              List<Request> rejectedRequests) {
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(RequestMapper.mapToParticipationRequestDto(confirmedRequests))
                .rejectedRequests(RequestMapper.mapToParticipationRequestDto(rejectedRequests))
                .build();
    }

    public List<EventShortDto> mapToEventShortDto(List<Event> events,
                                                  Map<Long, Long> views,
                                                  Map<Long, Long> confirmedRequests) {
        List<EventShortDto> dtos = new ArrayList<>();
        for (Event event : events) {
            Long eventViews = views.getOrDefault(event.getId(), 0L);
            Long eventConfirmedRequests = confirmedRequests.getOrDefault(event.getId(), 0L);
            dtos.add(mapToEventShortDto(event, eventViews, eventConfirmedRequests));
        }
        return dtos;
    }

    public List<EventFullDto> mapToEventFullDto(List<Event> events,
                                                Map<Long, Long> views,
                                                Map<Long, Long> confirmedRequests,
                                                Map<Long, List<Comment>> comments) {
        List<EventFullDto> dtos = new ArrayList<>();
        for (Event event : events) {
            Long eventViews = views.getOrDefault(event.getId(), 0L);
            Long eventConfirmedRequests = confirmedRequests.getOrDefault(event.getId(), 0L);
            List<Comment> eventComments = comments.getOrDefault(event.getId(), Collections.emptyList());
            List<CommentDto> eventCommentDtos = CommentMapper.mapToCommentDto(eventComments);
            dtos.add(mapToEventFullDto(event, eventViews, eventConfirmedRequests, eventCommentDtos));
        }
        return dtos;
    }
}
