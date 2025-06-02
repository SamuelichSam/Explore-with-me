package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repo.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repo.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public List<CompilationDto> findCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получение подборок событий");
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable).getContent();
        return compilations.stream()
                .map(compilationMapper::toDto)
                .toList();
    }

    @Override
    public CompilationDto findCompilationById(Long compId) {
        log.info("Получение подборки событий с id - {}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compId + " не найдена"));
        return compilationMapper.toDto(compilation);
    }

    @Transactional
    @Override
    public CompilationDto createCompilationAdmin(NewCompilationDto dto) {
        log.info("Добавление новой подборки (подборка может не содержать событий)");
        List<Long> eventIds = Optional.ofNullable(dto.events())
                .orElseGet(ArrayList::new);
        List<Event> events = eventRepository.findAllById(eventIds);
        Compilation compilation = compilationRepository.save(compilationMapper.toNewCompilation(dto, events));
        return compilationMapper.toDto(compilation);
    }

    @Transactional
    @Override
    public void deleteCompilationAdmin(Long compId) {
        log.info("Удаление подборки с id - {}", compId);
        compilationRepository.deleteById(compId);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilationAdmin(Long compId, UpdateCompilationRequest dto) {
        log.info("Обновление подборки с id - {}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compId + " не найдена"));
        if (dto.events() != null) {
            List<Event> events = eventRepository.findAllById(dto.events());
            compilation.setEvents(events);
        }
        if (dto.pinned() != null) {
            compilation.setPinned(dto.pinned());
        }
        if (dto.title() != null && !dto.title().isBlank()) {
            compilation.setTitle(dto.title());
        }
        return compilationMapper.toDto(compilationRepository.save(compilation));
    }
}
