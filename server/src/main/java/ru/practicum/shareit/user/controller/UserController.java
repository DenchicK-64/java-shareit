package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping(path = "/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        log.info("Пользователь добавлен: {}", userDto.getName());
        return userService.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Пользователь добавлен: {}", userDto.getName());
        return userService.update(userId, userDto);
    }

    @GetMapping
    public List<UserDto> findAll() {
        log.info("Получение всех пользователей");
        return userService.findAll();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        log.debug("Удаление пользователя");
        userService.delete(userId);
    }
}