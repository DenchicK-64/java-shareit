package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto create(long userId, ItemDto itemDto);

    ItemDto update(long userId, long itemId, ItemDto itemDto);

    List<ItemDto> findAll(long userId);

    ItemDto getItem(long itemId);

    void delete(long itemId);

    List<ItemDto> findItemByName(String text);
}