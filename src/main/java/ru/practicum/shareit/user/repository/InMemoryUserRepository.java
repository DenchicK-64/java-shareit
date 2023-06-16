package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UserAlreadyExistsException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long userId = 1L;

    @Override
    public User create(User user) {
        if (validateEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с этим email уже существует");
        }
        user.setId(userId++);
        users.put(user.getId(), user);
        log.info("Пользователь добавлен: {}", user.getName());
        return user;
    }

    @Override
    public User update(long userId, User user) {
        checkUser(userId);
        User updUser = getUser(userId);
        if (validateEmail(user.getEmail()) && (!updUser.getEmail().equals(user.getEmail()))) {
                throw new UserAlreadyExistsException("Пользователь с этим email уже существует");
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            updUser.setName(user.getName());
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            updUser.setEmail(user.getEmail());
        }
        users.put(userId, updUser);
        log.info("Пользователь добавлен: {}", user.getName());
        return updUser;
    }

    @Override
    public List<User> findAll() {
        log.info("Получение всех пользователей");
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUser(long userId) {
        checkUser(userId);
        return users.get(userId);
    }

    @Override
    public void delete(long userId) {
        users.remove(userId);
    }

    @Override
    public void checkUser(long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь не найден в базе данных");
        }
    }

    private boolean validateEmail(String email) {
        return users.values().stream().anyMatch(user -> user.getEmail().equals(email));
    }
}