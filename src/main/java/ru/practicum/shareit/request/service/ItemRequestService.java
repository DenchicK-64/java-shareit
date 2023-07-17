package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDtoWithItems> findAllByUserId(Long userId);

    List<ItemRequestDtoWithItems> findAll(Long userId, Integer from, Integer size);

    ItemRequestDtoWithItems getItemRequestById(Long userId, Long requestId);
}