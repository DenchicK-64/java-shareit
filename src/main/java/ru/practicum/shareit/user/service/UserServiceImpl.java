package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.mapper.UserMapper.toUser;
import static ru.practicum.shareit.user.mapper.UserMapper.toUserDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto create(UserDto userDto) {
        User user = toUser(userDto);
        User newUser = userRepository.save(user);
        return toUserDto(newUser);
    }

    @Transactional
    @Override
    public UserDto update(long userId, UserDto userDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id" + userId + "не найден в базе данных"));
        if (userDto.getEmail() != null & !user.getEmail().isBlank()) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null & !user.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        User updUser = userRepository.save(user);
        return toUserDto(updUser);
    }

    @Transactional
    @Override
    public List<UserDto> findAll() {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserDto getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id" + userId + "не найден в базе данных"));
        return toUserDto(user);
    }

    @Transactional
    @Override
    public void delete(long userId) {
        userRepository.deleteById(userId);
    }
}