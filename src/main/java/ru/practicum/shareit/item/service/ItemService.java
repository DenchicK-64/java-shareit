package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBooking;

import java.util.List;

public interface ItemService {

    ItemDto create(long userId, ItemDto itemDto);

    ItemDto update(long userId, long itemId, ItemDto itemDto);

    List<ItemResponseDtoWithBooking> findAll(long userId);

    ItemResponseDtoWithBooking getItem(long userId, long itemId);

    void delete(long itemId);

    List<ItemDto> findItemByName(String text);

    CommentDto createComment(long userId, long itemId, CommentDto commentDto);
}