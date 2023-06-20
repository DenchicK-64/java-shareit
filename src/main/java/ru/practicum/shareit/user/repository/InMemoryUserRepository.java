package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UserAlreadyExistsException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Slf4j
@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> userEmails = new HashSet<>();
    private long userId = 1L;

    @Override
    public User create(User user) {
        if (userEmails.contains(user.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с этим email уже существует");
        }
        user.setId(userId++);
        users.put(user.getId(), user);
        userEmails.add(user.getEmail());
        log.info("Пользователь добавлен: {}", user.getName());
        log.info("Список уникальных email после создания нового пользователя: {}", userEmails);
        return user;
    }

    @Override
    public User update(long userId, User user) {
        User updUser = getUser(userId);
        String oldEmail = getUser(userId).getEmail();
        if (userEmails.contains(user.getEmail()) && !updUser.getEmail().equals(user.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с этим email уже существует");
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            updUser.setName(user.getName());
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            updUser.setEmail(user.getEmail());
        }
        users.put(userId, updUser);
        userEmails.remove(oldEmail);
        userEmails.add(updUser.getEmail());
        log.info("Пользователь добавлен: {}", user.getName());
        log.info("Список уникальных email после апдейта пользователя: {}", userEmails);
        return updUser;
    }

    @Override
    public List<User> findAll() {
        log.info("Получение всех пользователей");
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUser(long userId) {
        User user = users.get(userId);
        if (user != null) {
            return user;
        }
        throw new NotFoundException("Пользователь не найден в базе данных");
    }

    @Override
    public void delete(long userId) {
        String email = getUser(userId).getEmail();
        userEmails.remove(email);
        users.remove(userId);
    }
}