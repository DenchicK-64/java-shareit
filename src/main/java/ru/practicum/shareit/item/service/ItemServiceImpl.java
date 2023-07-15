package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotAvailableException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.OperationAccessException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBooking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.mapper.ItemMapper.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final Clock clock;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id" + userId + "не найден в базе данных"));
        ItemRequest itemRequest = null;
        if (itemDto.getRequestId() != null) {
            itemRequest = itemRequestRepository.findById(itemDto.getRequestId()).orElseThrow(() ->
                    new NotFoundException("Запрос с id " + itemDto.getRequestId() + " не найден в базе данных"));
        }
        Item item = toItem(itemDto, user, itemRequest);
        Item newItem = itemRepository.save(item);
        log.info("Создана вещь: " + item);
        return toItemDto(newItem);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item updItem = itemRepository.findItemsByIdAndOwnerId(itemId, userId);
        if (updItem == null) {
            throw new NotFoundException("Вещь с id" + itemId + "не найдена в базе данных");
        }
        if (!Objects.equals(updItem.getOwner().getId(), userId)) {
            throw new OperationAccessException("Нельзя выполнить обновление: пользователь не является собственником вещи");
        }
        if (itemDto.getName() != null) {
            updItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            updItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            updItem.setAvailable(itemDto.getAvailable());
        }
        Item updItem1 = itemRepository.save(updItem);
        return toItemDto(updItem1);
    }

    @Override
    public List<ItemResponseDtoWithBooking> findAll(Long userId, Integer from, Integer size) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id" + userId + "не найден в базе данных"));
        if (from < 0 || size <= 0) {
            throw new ValidationException("Индекс первого элемента не может быть отрицательным и количество отображаемых элементов должно быть больше 0");
        }
        PageRequest pageRequest = PageRequest.of(from/size, size);
        List<Item> items = itemRepository.findAllByOwnerId(userId, pageRequest);
        List<ItemResponseDtoWithBooking> itemResponseDtoWithBookingList = new ArrayList<>();
        for (Item item : items) {
            List<CommentDto> comments = CommentMapper.toCommentDtoList(commentRepository.findAllByItemId(item.getId()));
            itemResponseDtoWithBookingList.add(toItemResponseDtoWithBooking(item, null, null, comments));
        }
        return itemResponseDtoWithBookingList.stream()
                .peek(this::setBookings)
                .collect(Collectors.toList());
    }

    @Override
    public ItemResponseDtoWithBooking getItem(Long userId, Long itemId) {
        ItemResponseDtoWithBooking itemResponseDtoWithBooking;
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id" + itemId + "не найдена в базе данных"));
        List<CommentDto> comments = CommentMapper.toCommentDtoList(commentRepository.findAllByItemId(itemId));
        itemResponseDtoWithBooking = ItemMapper.toItemResponseDtoWithBooking(item, null, null, comments);
        if (Objects.equals(item.getOwner().getId(), userId)) {
            return setBookings(itemResponseDtoWithBooking);
        } else {
            return itemResponseDtoWithBooking;
        }
    }

    @Override
    public void delete(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    public List<ItemDto> findItemByName(String text, Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            throw new ValidationException("Индекс первого элемента не может быть отрицательным и количество отображаемых элементов должно быть больше 0");
        }
        PageRequest pageRequest = PageRequest.of(from/size, size);
        if (text != null && !text.isBlank()) {
            List<Item> allItems = itemRepository.search(text, pageRequest);
            return allItems.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        LocalDateTime time = LocalDateTime.now(clock);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id" + userId + "не найден в базе данных"));
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Вещь с id" + itemId + "не найдена в базе данных"));
        Booking booking =
                bookingRepository.findFirstByBookerAndItemAndEndIsBeforeOrderByEndDesc(user, item, time);
        log.info(String.valueOf(booking));
        if (booking == null) {
            throw new NotAvailableException("Пользователь с " + userId + " не бронировал вещь с " + itemId);
        }
        Comment comment = CommentMapper.toComment(commentDto, user, item, time);
        log.info(String.valueOf(comment));
        Comment newComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(newComment);
    }

    private ItemResponseDtoWithBooking setBookings(ItemResponseDtoWithBooking itemResponseDtoWithBooking) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Booking> bookings = bookingRepository.findAllBookingsItem(itemResponseDtoWithBooking.getId());
        Booking lastBooking = bookings.stream()
                .filter(obj -> !(obj.getStatus().equals(Status.REJECTED)))
                .filter(obj -> obj.getStart().isBefore(now))
                .min((obj1, obj2) -> obj2.getStart().compareTo(obj1.getStart())).orElse(null);
        Booking nextBooking = bookings.stream()
                .filter(obj -> !(obj.getStatus().equals(Status.REJECTED)))
                .filter(obj -> obj.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart)).orElse(null);
        if (lastBooking != null) {
            itemResponseDtoWithBooking.setLastBooking(BookingMapper.toShortBookingDto(lastBooking));
        }
        if (nextBooking != null) {
            itemResponseDtoWithBooking.setNextBooking(BookingMapper.toShortBookingDto(nextBooking));
        }
        log.info(String.valueOf(itemResponseDtoWithBooking));
        return itemResponseDtoWithBooking;
    }
}