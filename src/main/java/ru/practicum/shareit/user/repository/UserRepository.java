package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    User create(User user);

    User update(long userId, User user);

    List<User> findAll();

    User getUser(long userId);

    void delete(long userId);
}