package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repo.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> findUsers(List<Integer> ids, Integer from, Integer size) {
        log.info("Получение информации о пользователях");
        Pageable pageable = PageRequest.of(from / size, size);
        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(pageable).getContent();
        } else {
            users = userRepository.findByIdIn(ids, pageable).getContent();
        }
        return users.stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Добавление нового пользователя");
        if (userRepository.existsByEmail(userDto.email())) {
            throw new ConflictException("Пользователь с такой почтой уже существует");
        }
        User user = userRepository.save(userMapper.toUser(userDto));
        return userMapper.toDto(user);
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с id - {}", id);
        userRepository.deleteById(id);
    }
}
