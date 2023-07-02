package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(long userId, BookingRequestDto bookingRequestDto);

    BookingResponseDto approve(long userId, long bookingId, boolean approved);

    BookingResponseDto getBooking(long userId, long bookingId);

    List<BookingResponseDto> findAllByBookerId(long userId, String state);

    List<BookingResponseDto> findAllByOwnerId(long ownerId, String state);
}