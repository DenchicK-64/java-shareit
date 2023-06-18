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
    private final Map<String, Long> userEmails = new HashMap<>();
    private long userId = 1L;

    @Override
    public User create(User user) {
        if (userEmails.containsKey(user.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с этим email уже существует");
        }
        user.setId(userId++);
        users.put(user.getId(), user);
        userEmails.put(user.getEmail(), user.getId());
        log.info("Пользователь добавлен: {}", user.getName());
        System.out.println(userEmails);
        return user;
    }

    @Override
    public User update(long userId, User user) {
        User updUser = getUser(userId);
        String oldEmail = getUser(userId).getEmail();
        if (hasEmailDuplicates(user.getEmail(), userId)) {
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
        userEmails.put(updUser.getEmail(), userId);
        log.info("Пользователь добавлен: {}", user.getName());
        System.out.println(userEmails);
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

    private boolean hasEmailDuplicates(String email, long userId) {
        return userEmails.containsKey(email) && userId != userEmails.get(email);
    }
}