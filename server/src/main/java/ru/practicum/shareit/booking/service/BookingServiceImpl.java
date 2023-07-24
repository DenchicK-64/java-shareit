package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.booking.mapper.BookingMapper;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingResponseDto create(Long userId, BookingRequestDto bookingRequestDto) {
        checkTimeConflict(bookingRequestDto.getStart(), bookingRequestDto.getEnd());
        User booker = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден в базе данных"));
        Long itemId = bookingRequestDto.getItemId();
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Вещь с id " + itemId + " не найдена в базе данных"));
        checkAvailabilityForBooking(item);
        if (Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException("Владелец вещи не может её сам забронировать");
        }
        Booking booking = BookingMapper.toBooking(bookingRequestDto, booker, item);
        Booking newBooking = bookingRepository.save(booking);
        log.info("Пользователь с id = " + userId +
                " создал запрос на бронирование вещи с id = " + itemId +
                " от арендодателя с id = " + item.getOwner().getId() + ". id бронирования = " + newBooking.getId() +
                ", статус бронирования - " + newBooking.getStatus());
        return BookingMapper.toBookingResponseDto(newBooking);
    }

    @Override
    public BookingResponseDto approve(Long userId, Long bookingId, Boolean approved) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден в базе данных"));
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException("Запрос на бронирование с id " + bookingId + " не найден в базе данных"));
        if (!booking.getStart().isAfter(LocalDateTime.now())) {
            throw new TimeConflictException("Время начала брони не может быть раньше текущего");
        }
        if (!userId.equals(booking.getItem().getOwner().getId())) {
            throw new NotFoundException("Пользователь с id= " + userId + " не является владельцем вещи с id=" + booking.getItem().getId());
        }
        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new NotAvailableException("Решение по данному запросу на бронирование уже принято");
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        Booking newBooking = bookingRepository.save(booking);
        log.info("Пользователь с id = " + newBooking.getBooker().getId() +
                " получил ответ за запрос на бронирование вещи с id = " + newBooking.getItem().getId() +
                " от арендодателя с id = " + userId + ". id бронирования = " + newBooking.getId() +
                ", статус бронирования - " + newBooking.getStatus());
        return BookingMapper.toBookingResponseDto(newBooking);
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден в базе данных"));
        return bookingRepository.findById(bookingId)
                .filter(b -> Objects.equals(b.getBooker().getId(), userId) || Objects.equals(b.getItem().getOwner().getId(), userId))
                .map(BookingMapper::toBookingResponseDto)
                .orElseThrow(() -> new NotFoundException("Просматривать бронирование может либо владелец вещи, либо автор бронирования"));
    }

    @Override
    public List<BookingResponseDto> findAllByBookerId(Long userId, String state, Integer from, Integer size) {
        User booker = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден в базе данных"));
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(from / size, size);
        switch (state) {
            case "ALL":
                bookings.addAll(bookingRepository.findAllByBookerIdOrderByStartDesc(userId, pageRequest));
                log.info("Поиск всех запросов пользователя c id = " + userId + ", state = ALL, количество = " + bookings.size());
                break;
            case "CURRENT":
                bookings.addAll(bookingRepository.findAllByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(userId, now, now, pageRequest));
                log.info("Поиск всех запросов пользователя c id = " + userId + ", state = CURRENT, количество = " + bookings.size());
                break;
            case "PAST":
                bookings.addAll(bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(userId, now, pageRequest));
                log.info("Поиск всех запросов пользователя c id = " + userId + ", state = PAST, количество = " + bookings.size());
                break;
            case "FUTURE":
                bookings.addAll(bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(userId, now, pageRequest));
                log.info("Поиск всех запросов пользователя c id = " + userId + ", state = FUTURE, количество = " + bookings.size());
                break;
            case "WAITING":
                bookings.addAll(bookingRepository.findAllByBookerIdAndStartIsAfterAndStatusIsOrderByStartDesc(userId, now,
                        Status.WAITING, pageRequest));
                log.info("Поиск всех запросов пользователя c id = " + userId + ", state = WAITING, количество = " + bookings.size());
                break;
            case "REJECTED":
                bookings.addAll(bookingRepository.findAllByBookerIdAndStatusIsOrderByStartDesc(userId, Status.REJECTED, pageRequest));
                log.info("Поиск всех запросов пользователя c id = " + userId + ", state = REJECTED, количество = " + bookings.size());
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }
        if (bookings.isEmpty()) {
            throw new NotFoundException("Пользователь " + userId + " ещё не совершал операций по бронированию вещей");
        }
        return BookingMapper.toBookingResponseDtoList(bookings);
    }

    @Override
    public List<BookingResponseDto> findAllByOwnerId(Long ownerId, String state, Integer from, Integer size) {
        User owner = userRepository.findById(ownerId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + ownerId + " не найден в базе данных"));
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;
        PageRequest pageRequest = PageRequest.of(from / size, size);
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findAllBookingsByOwnerId(ownerId, pageRequest);
                break;
            case "CURRENT":
                bookings = bookingRepository.findAllBookingsByOwnerIdWithStatusCurrent(ownerId, now, pageRequest);
                break;
            case "PAST":
                bookings = bookingRepository.findAllBookingsByOwnerIdWithStatusPast(ownerId, now, pageRequest);
                break;
            case "FUTURE":
                bookings = bookingRepository.findAllBookingsByOwnerIdWithStatusFuture(ownerId, now, pageRequest);
                break;
            case "WAITING":
                bookings = bookingRepository.findAllBookingsByOwnerIdWithStatusWaiting(ownerId, now,
                        Status.WAITING, pageRequest);
                break;
            case "REJECTED":
                bookings = bookingRepository.findAllBookingsByOwnerIdWithStatusRejected(ownerId, Status.REJECTED, pageRequest);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }
        if (bookings.isEmpty()) {
            throw new NotFoundException("Отсутствуют операции по бронированию вещей пользователя " + ownerId);
        }
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    private void checkAvailabilityForBooking(Item item) {
        if (!item.getAvailable()) {
            throw new NotAvailableException("Вещь с id " + item.getId() + " в данный момент не доступна для бронирования");
        }
    }

    private void checkTimeConflict(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end) || start.equals(end)) {
            throw new TimeConflictException("Дата окончания бронирования не может быть раньше даты начала бронирования или равна ей");
        }
    }
}