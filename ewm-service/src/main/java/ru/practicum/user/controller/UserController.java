package ru.practicum.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> findUsers(@RequestParam(required = false) List<Integer> ids,
                                   @RequestParam(defaultValue = "0") Integer from,
                                   @RequestParam(defaultValue = "10") Integer size) {
        return userService.findUsers(ids, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserDto dto) {
        return userService.createUser(dto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}
