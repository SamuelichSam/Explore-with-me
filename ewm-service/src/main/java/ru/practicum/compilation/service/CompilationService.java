package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    List<CompilationDto> findCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto findCompilationById(Long compId);

    CompilationDto createCompilationAdmin(NewCompilationDto dto);

    void deleteCompilationAdmin(Long compId);

    CompilationDto updateCompilationAdmin(Long compId, UpdateCompilationRequest dto);
}
