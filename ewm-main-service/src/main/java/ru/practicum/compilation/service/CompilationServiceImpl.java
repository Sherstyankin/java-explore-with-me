package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.request.NewCompilationDto;
import ru.practicum.dto.request.UpdateCompilationRequest;
import ru.practicum.entity.Compilation;
import ru.practicum.entity.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.request.service.EventRequestService;
import ru.practicum.view.ViewService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRequestService eventRequestService;
    private final EventRepository eventRepository;
    private final ViewService viewService;

    @Override
    public CompilationDto addCompilation(NewCompilationDto dto) {
        Compilation compilation;
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(dto.getEvents());
            compilation = CompilationMapper.mapToCompilation(dto, events);
            List<Long> eventIds = new ArrayList<>(dto.getEvents());
            Map<Long, Long> views = viewService.getViews(eventIds);
            Map<Long, Long> confirmedRequests = eventRequestService.getConfirmedRequests(eventIds);
            Compilation response = compilationRepository.save(compilation);
            return CompilationMapper.mapToCompilationDto(response, views, confirmedRequests);
        } else {
            compilation = CompilationMapper.mapToCompilation(dto, null);
            Compilation response = compilationRepository.save(compilation);
            return CompilationMapper.mapToCompilationDto(response);
        }
    }

    @Override
    public void deleteCompilation(Long compId) {
        findCompilation(compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        Compilation compilation = findCompilation(compId);
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            compilation.setTitle(dto.getTitle());
        }
        compilation.setPinned(dto.isPinned());
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(dto.getEvents());
            compilation.setEvents(events);
            Map<Long, Long> views = viewService.getViews(dto.getEvents());
            Map<Long, Long> confirmedRequests = eventRequestService.getConfirmedRequests(dto.getEvents());
            return CompilationMapper.mapToCompilationDto(compilationRepository.save(compilation),
                    views,
                    confirmedRequests);
        }
        return CompilationMapper.mapToCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable page = PageRequest.of(from, size, Sort.by("id"));
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(page).getContent();
        } else {
            compilations = compilationRepository.findByPinned(pinned, page);
        }
        Map<Long, Map<Long, Long>> viewsGroupedByCompId = compilations.stream()
                .collect(Collectors.toMap(Compilation::getId,
                        compilation -> viewService.getViews(compilation.getEvents().stream()
                                .map(Event::getId)
                                .collect(Collectors.toList()))));
        Map<Long, Map<Long, Long>> confirmedRequestsGroupedByCompId = compilations.stream()
                .collect(Collectors.toMap(Compilation::getId,
                        compilation -> eventRequestService.getConfirmedRequests(compilation.getEvents().stream()
                                .map(Event::getId)
                                .collect(Collectors.toList()))));
        return CompilationMapper.mapToCompilationDto(compilations,
                viewsGroupedByCompId,
                confirmedRequestsGroupedByCompId);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = findCompilation(compId);
        if (compilation.getEvents() != null && !compilation.getEvents().isEmpty()) {
            List<Long> eventIds = compilation.getEvents().stream()
                    .map(Event::getId)
                    .collect(Collectors.toList());
            Map<Long, Long> views = viewService.getViews(eventIds);
            Map<Long, Long> confirmedRequests = eventRequestService.getConfirmedRequests(eventIds);
            return CompilationMapper.mapToCompilationDto(compilation, views, confirmedRequests);
        }
        return CompilationMapper.mapToCompilationDto(compilation);
    }

    private Compilation findCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException("Подборка c ID:" + compId + " не найдена."));
    }
}
