package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBooking;

import java.util.List;

public interface ItemService {

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    List<ItemResponseDtoWithBooking> findAll(Long userId, Integer from, Integer size);

    ItemResponseDtoWithBooking getItem(Long userId, Long itemId);

    void delete(Long itemId);

    List<ItemDto> findItemByName(String text, Integer from, Integer size);

    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);
}