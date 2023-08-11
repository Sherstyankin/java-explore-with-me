package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.request.NewCompilationDto;
import ru.practicum.entity.Compilation;
import ru.practicum.entity.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {

    public Compilation mapToCompilation(NewCompilationDto dto, List<Event> events) {
        return Compilation.builder()
                .pinned(dto.isPinned())
                .title(dto.getTitle())
                .events(events)
                .build();
    }

    public CompilationDto mapToCompilationDto(Compilation compilation,
                                              Map<Long, Long> views,
                                              Map<Long, Long> confirmedRequests) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.isPinned())
                .title(compilation.getTitle())
                .events(EventMapper.mapToEventShortDto(new ArrayList<>(compilation.getEvents()), views, confirmedRequests))
                .build();
    }

    public CompilationDto mapToCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.isPinned())
                .title(compilation.getTitle())
                .events(Collections.emptyList())
                .build();
    }

    public List<CompilationDto> mapToCompilationDto(List<Compilation> compilations,
                                                    Map<Long, Map<Long, Long>> viewsGroupedByCompId,
                                                    Map<Long, Map<Long, Long>> confirmedRequestsGroupedByCompId) {
        if (compilations == null || compilations.isEmpty()) {
            return Collections.emptyList();
        }
        return compilations.stream()
                .map(compilation -> mapToCompilationDto(compilation,
                        viewsGroupedByCompId.get(compilation.getId()),
                        confirmedRequestsGroupedByCompId.get(compilation.getId())))
                .collect(Collectors.toList());
    }
}
