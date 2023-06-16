package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.OperationAccessException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long itemId = 1L;

    @Override
    public Item create(Item item) {
        item.setId(itemId++);
        items.put(item.getId(), item);
        log.info("Пользователь добавлен: {}", item.getName());
        return item;
    }

    @Override
    public Item update(long itemId, Item item) {
        checkItem(itemId);
        Item updItem = getItem(itemId);
        if (!updItem.getOwner().equals(item.getOwner())) {
            throw new OperationAccessException("Нельзя выполнить обновление: пользователь не является собственником вещи");
        }
        if (item.getName() != null) {
            updItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            updItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            updItem.setAvailable(item.getAvailable());
        }
        items.put(itemId, updItem);
        log.info("Пользователь добавлен: {}", updItem.getName());
        return updItem;
    }

    @Override
    public List<Item> findAll(long userId) {
        log.info("Получение всех вещей пользователя");
        return items.values().stream()
                .filter(Item -> Objects.equals(Item.getOwner().getId(), userId))
                .collect(Collectors.toList());
    }

    @Override
    public Item getItem(long itemId) {
        checkItem(itemId);
        return items.get(itemId);
    }

    @Override
    public void delete(long itemId) {
        items.remove(itemId);
    }

    @Override
    public List<Item> findItemByName(String text) {
        return items.values().stream().filter(Item -> Item.getAvailable() && (Item.getName().toLowerCase().contains(text.toLowerCase())
                        || Item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .collect(Collectors.toList());
    }

    @Override
    public void checkItem(long itemId) {
        if (!items.containsKey(itemId)) {
            throw new NotFoundException("Вещь не найдена в базе данных");
        }
    }
}