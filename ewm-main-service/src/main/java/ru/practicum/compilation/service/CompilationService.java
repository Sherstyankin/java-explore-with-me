package ru.practicum.compilation.service;

import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.request.NewCompilationDto;
import ru.practicum.dto.request.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilation(NewCompilationDto dto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto);

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationById(Long compId);
}
