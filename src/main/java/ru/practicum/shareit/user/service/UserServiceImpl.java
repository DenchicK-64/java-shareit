package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.mapper.UserMapper.toUser;
import static ru.practicum.shareit.user.mapper.UserMapper.toUserDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        User user = toUser(userDto);
        User newUser = userRepository.create(user);
        return toUserDto(newUser);
    }

    @Override
    public UserDto update(long userId, UserDto userDto) {
        User user = toUser(userDto);
        User updUser = userRepository.update(userId, user);
        return toUserDto(updUser);
    }

    @Override
    public List<UserDto> findAll() {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getUser(long userId) {
        User user = userRepository.getUser(userId);
        return toUserDto(user);
    }

    @Override
    public void delete(long userId) {
        userRepository.delete(userId);
    }
}