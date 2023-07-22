package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBooking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemMapperTest {
    User testUser = new User(1L, "Test_User", "mail@somemail.ru");
    User testUserTwo = new User(2L, "Test_User2", "mail2@somemail.ru");
    ItemRequest itemRequest = new ItemRequest(1L, "Test_Description", testUser,
            LocalDateTime.now().plusHours(3));
    Item testItem = new Item(1L, "Test_Name", "Test_Description", true, testUserTwo, itemRequest);
    ItemDto testItemDto = new ItemDto(1L, "Test_Name", "Test_Description", true, itemRequest.getId());
    Item testItemTwo = new Item(2L, "Test_Name2", "Test_Description2", true, testUser, null);
    Comment commentOne = new Comment(1L, "some text", testItem, testUserTwo, LocalDateTime.now());
    CommentDto commentOneDto = new CommentDto(1L, "some text", testItem.getId(), "Test_User2",LocalDateTime.now());
    Booking lastBooking = new Booking(1L, LocalDateTime.now().minusMinutes(20),
            LocalDateTime.now().plusMinutes(20), testItem, testUserTwo, Status.WAITING);
    Booking nextBooking = new Booking(2L, LocalDateTime.now().plusDays(8),
            LocalDateTime.now().plusDays(10), testItem, testUserTwo, Status.WAITING);
    ShortBookingDto lastBookingShort = new ShortBookingDto(lastBooking.getId(), lastBooking.getBooker().getId(),
            lastBooking.getStart(), lastBooking.getEnd());
    ShortBookingDto nextBookingShort = new ShortBookingDto(nextBooking.getId(), nextBooking.getBooker().getId(),
            nextBooking.getStart(), nextBooking.getEnd());
    ItemResponseDtoWithBooking itemResponseDtoWithBooking = new ItemResponseDtoWithBooking(1L, "Test_Name",
            "Test_Description", true, lastBookingShort, nextBookingShort, List.of(commentOneDto));

    @Test
    public void toItemDtoTest() {
        ItemDto actualItem = ItemMapper.toItemDto(testItem);
        assertEquals(actualItem, testItemDto);
    }

    @Test
    public void toItemTest() {
        Item actualItem = ItemMapper.toItem(testItemDto, testUserTwo, itemRequest);
        assertEquals(actualItem, testItem);
    }

    @Test
    public void toItemResponseDtoWithBookingTest() {
        ItemResponseDtoWithBooking actualItem = ItemMapper.toItemResponseDtoWithBooking(testItem, lastBookingShort, nextBookingShort, List.of(commentOneDto));
        assertEquals(actualItem, itemResponseDtoWithBooking);
    }
}