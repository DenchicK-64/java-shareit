package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotAvailableException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.OperationAccessException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBooking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.mapper.ItemMapper.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id" + userId + "не найден в базе данных"));
        Item item = toItem(itemDto, user);
        Item newItem = itemRepository.save(item);
        return toItemDto(newItem);
    }

    @Transactional
    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        Item updItem = itemRepository.findItemsByIdAndOwnerId(itemId, userId);
        if (updItem == null) {
            throw new NotFoundException("Вещь с id" + itemId + "не найдена в базе данных");
        }
        if (updItem.getOwner().getId() != userId) {
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

    @Transactional
    @Override
    public List<ItemResponseDtoWithBooking> findAll(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id" + userId + "не найден в базе данных"));
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        List<ItemResponseDtoWithBooking> itemResponseDtoWithBookingList = new ArrayList<>();
        for (Item item : items) {
            List<CommentDto> comments = CommentMapper.toCommentDtoList(commentRepository.findAllByItemId(item.getId()));
            itemResponseDtoWithBookingList.add(toItemResponseDtoWithBooking(item, null, null, comments));
        }
        return itemResponseDtoWithBookingList.stream()
                .peek((i) -> CommentMapper.toCommentDtoList(commentRepository.findAllByItemId(i.getId())))
                .peek(this::setBookings)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ItemResponseDtoWithBooking getItem(long userId, long itemId) {
        ItemResponseDtoWithBooking itemResponseDtoWithBooking;
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id" + itemId + "не найдена в базе данных"));
        List<CommentDto> comments = CommentMapper.toCommentDtoList(commentRepository.findAllByItemId(itemId));
        itemResponseDtoWithBooking = ItemMapper.toItemResponseDtoWithBooking(item, null, null, comments);
        if (item.getOwner().getId() == userId) {
            return setBookings(itemResponseDtoWithBooking);
        } else {
            return itemResponseDtoWithBooking;
        }
    }

    @Transactional
    @Override
    public void delete(long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional
    public List<ItemDto> findItemByName(String text) {
        if (text != null && !text.isBlank()) {
            List<Item> allItems = itemRepository.search(text);
            return allItems.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public CommentDto createComment(long userId, long itemId, CommentDto commentDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id" + userId + "не найден в базе данных"));
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Вещь с id" + itemId + "не найдена в базе данных"));
        LocalDateTime now = LocalDateTime.now();
        Booking booking =
                bookingRepository.findFirstByBookerAndItemAndEndIsBeforeOrderByEndDesc(user, item, now);
        log.info(String.valueOf(booking));
        if (booking == null) {
            throw new NotAvailableException("Пользователь с " + userId + " не бронировал вещь с " + itemId);
        }
        Comment comment = CommentMapper.toComment(commentDto, user, item);
        log.info(String.valueOf(comment));
        Comment newComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(newComment);
    }

    private ItemResponseDtoWithBooking setBookings(ItemResponseDtoWithBooking itemResponseDtoWithBooking) {
        LocalDateTime now = LocalDateTime.now();
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