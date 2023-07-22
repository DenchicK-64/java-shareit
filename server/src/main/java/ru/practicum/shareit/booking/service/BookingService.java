package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(Long userId, BookingRequestDto bookingRequestDto);

    BookingResponseDto approve(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto getBooking(Long userId, Long bookingId);

    List<BookingResponseDto> findAllByBookerId(Long userId, String state, Integer from, Integer size);

    List<BookingResponseDto> findAllByOwnerId(Long ownerId, String state, Integer from, Integer size);
}