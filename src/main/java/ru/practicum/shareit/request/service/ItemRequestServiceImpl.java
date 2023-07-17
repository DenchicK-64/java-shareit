package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден в базе данных"));
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        ItemRequest newItemRequest = itemRequestRepository.save(itemRequest);
        log.info("Пользователь с id = " + userId + " создал запрос: " + itemRequest);
        return ItemRequestMapper.toItemRequestDto(newItemRequest);
    }

    @Override
    public List<ItemRequestDtoWithItems> findAllByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден в базе данных"));
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);
        return convertToItemRequestDtoWithItemsList(itemRequests);
    }

    @Override
    public List<ItemRequestDtoWithItems> findAll(Long userId, Integer from, Integer size) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден в базе данных"));
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("created"));
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdIsNot(userId, pageRequest);
        return convertToItemRequestDtoWithItemsList(itemRequests);
    }

    @Override
    public ItemRequestDtoWithItems getItemRequestById(Long userId, Long requestId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден в базе данных"));
        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос с id " + requestId + " не найден в базе данных"));
        List<ItemDto> items = setItems(itemRequest);
        ItemRequestDtoWithItems itemRequestDtoWithItems = ItemRequestMapper.toItemRequestDtoWithItems(itemRequest, new ArrayList<>());
        if (!items.isEmpty()) {
            itemRequestDtoWithItems.setItems(items);
        }
        return itemRequestDtoWithItems;
    }

    private List<ItemDto> setItems(ItemRequest itemRequest) {
        List<ItemDto> items = itemRepository.findAllByItemRequest(itemRequest).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        return items;
    }

    private List<ItemRequestDtoWithItems> convertToItemRequestDtoWithItemsList(List<ItemRequest> itemRequests) {
        List<ItemRequestDtoWithItems> itemRequestDtoWithItemsList = new ArrayList<>();
        for (ItemRequest itemRequest : itemRequests) {
            List<ItemDto> items = setItems(itemRequest);
            ItemRequestDtoWithItems itemRequestDtoWithItems = ItemRequestMapper.toItemRequestDtoWithItems(itemRequest, new ArrayList<>());
            if (!items.isEmpty()) {
                itemRequestDtoWithItems.setItems(items);
            }
            itemRequestDtoWithItemsList.add(itemRequestDtoWithItems);
        }
        return itemRequestDtoWithItemsList;
    }
}