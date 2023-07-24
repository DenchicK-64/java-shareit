package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Запрос вещи добавлен. id запроса = " + itemRequestDto.getId());
        return itemRequestService.create(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDtoWithItems> findAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получение пользователем с id = " + userId + "списка своих запросов");
        return itemRequestService.findAllByUserId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDtoWithItems> findAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                  @RequestParam Integer from,
                                  @RequestParam Integer size) {
        log.info("Получение списка запросов, созданных другими пользователями");
        return itemRequestService.findAll(userId, from, size);

    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoWithItems getItemRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long requestId) {
        log.info("Получение запроса с id = " + requestId);
        return itemRequestService.getItemRequestById(userId, requestId);
    }
}