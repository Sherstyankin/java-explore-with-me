package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.entity.Event;
import ru.practicum.entity.Request;
import ru.practicum.entity.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RequestMapper {

    public Request mapToRequest(Event event, User user) {
        return Request.builder()
                .event(event)
                .requester(user)
                .build();
    }

    public ParticipationRequestDto mapToParticipationRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }

    public List<ParticipationRequestDto> mapToParticipationRequestDto(List<Request> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }
        return requests.stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
    }
}
