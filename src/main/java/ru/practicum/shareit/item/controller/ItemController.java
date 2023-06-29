package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBooking;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") long userId, @Valid @RequestBody ItemDto itemDto) {
        log.info("Вещь добавлена: {}", itemDto.getName());
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId, @RequestBody ItemDto itemDto) {
        log.info("Вещь обновлена: {}", itemDto.getName());
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping
    public List<ItemResponseDtoWithBooking> findAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Получение всех вещей пользователя");
        return itemService.findAll(userId);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDtoWithBooking getItemById(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId) {
        return itemService.getItem(userId, itemId);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable long itemId) {
        log.debug("Удаление вещи");
        itemService.delete(itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> findItemByName(String text) {
        log.debug("Поиск вещи по её названию");
        return itemService.findItemByName(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @PathVariable long itemId,
                                    @Valid @RequestBody CommentDto commentDto) {
        log.info("Добавление отзыва");
        return itemService.createComment(userId, itemId, commentDto);
    }
}