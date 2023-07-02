package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto create(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @Valid @RequestBody BookingRequestDto bookingRequestDto) {
        log.info("Запрос на бронирование создан");
        return bookingService.create(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @PathVariable long bookingId,
                                      @RequestParam boolean approved) {
        log.info("Подтверждение или отклонение запроса на бронировании");
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable long bookingId) {
        log.info("Получение данных о бронировании по id");
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> findAllByBookerId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                      @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получение списка всех бронирований пользователя по его id");
        return bookingService.findAllByBookerId(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> findAllByOwnerId(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                     @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получение списка бронирований для всех вещей пользователя по его id");
        return bookingService.findAllByOwnerId(ownerId, state);
    }
}