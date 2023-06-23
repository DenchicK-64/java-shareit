package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.mapper.ItemMapper.toItem;
import static ru.practicum.shareit.item.mapper.ItemMapper.toItemDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        User user = userRepository.getUser(userId);
        Item item = toItem(itemDto, user);
        Item newItem = itemRepository.create(item);
        return toItemDto(newItem);
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        User user = userRepository.getUser(userId);
        Item item = toItem(itemDto, user);
        Item updItem = itemRepository.update(itemId, item);
        return toItemDto(updItem);
    }

    @Override
    public List<ItemDto> findAll(long userId) {
        User user = userRepository.getUser(userId);
        List<Item> allItems = itemRepository.findAll(user.getId());
        return allItems.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public ItemDto getItem(long itemId) {
        Item item = itemRepository.getItem(itemId);
        return toItemDto(item);
    }

    @Override
    public void delete(long itemId) {
        itemRepository.delete(itemId);
    }

    @Override
    public List<ItemDto> findItemByName(String text) {
        if (text != null && !text.isBlank()) {
            List<Item> allItems = itemRepository.findItemByName(text);
            return allItems.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}