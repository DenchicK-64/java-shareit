package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    Item create(Item item);

    Item update(long itemId, Item item);

    List<Item> findAll(long userId);

    Item getItem(long itemId);

    void delete(long itemId);

    List<Item> findItemByName(String text);
}