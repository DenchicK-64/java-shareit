package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BookingMapperTest {
    @Mock
    private Clock clock;
    private static final ZonedDateTime NOW_ZDT = ZonedDateTime.of(
            2023,
            7,
            10,
            10,
            10,
            10,
            0,
            ZoneId.of("UTC")
    );
    private LocalDateTime now = LocalDateTime.of(
            2023,
            7,
            10,
            10,
            10,
            10);
    User testUser = new User(1L, "Test_User", "mail@somemail.ru");
    User testUserTwo = new User(2L, "Test_User2", "mail2@somemail.ru");
    UserDto testUserTwoDto = new UserDto(2L, "Test_User2", "mail2@somemail.ru");
    ItemRequest itemRequest = new ItemRequest(1L, "Test_Description", testUser,
            LocalDateTime.now().plusHours(3));
    Item testItem = new Item(1L, "Test_Name", "Test_Description", true, testUserTwo, itemRequest);
    ItemDto testItemDto = new ItemDto(1L, "Test_Name", "Test_Description", true, itemRequest.getId());
    Booking lastBooking = new Booking(1L, now.minusMinutes(20),
            now.plusMinutes(20), testItem, testUserTwo, Status.WAITING);
    Booking nextBooking = new Booking(2L, now.plusDays(8),
            now.plusDays(10), testItem, testUserTwo, Status.WAITING);
    ShortBookingDto lastBookingShort = new ShortBookingDto(lastBooking.getId(), lastBooking.getBooker().getId(),
            lastBooking.getStart(), lastBooking.getEnd());
    BookingResponseDto lastBookingResponseDto = new BookingResponseDto(lastBooking.getId(), lastBooking.getStart(),
            lastBooking.getEnd(), testItemDto, testUserTwoDto, Status.WAITING);
    BookingResponseDto nextBookingResponseDto = new BookingResponseDto(nextBooking.getId(), nextBooking.getStart(),
            nextBooking.getEnd(), testItemDto, testUserTwoDto, Status.WAITING);
    BookingRequestDto lastBooKingRequestDto = new BookingRequestDto(lastBooking.getId(), testItem.getId(),
            lastBooking.getStart(), lastBooking.getEnd(), Status.WAITING);

    @Test
    public void toBookingResponseDtoTest() {
        BookingResponseDto actualBooking = BookingMapper.toBookingResponseDto(lastBooking);
        assertEquals(actualBooking, lastBookingResponseDto);
    }

    @Test
    public void toBookingTest() {
        Booking actualBooking = BookingMapper.toBooking(lastBooKingRequestDto, testUserTwo, testItem);
        assertEquals(actualBooking, lastBooking);
    }

    @Test
    public void toShortBookingDtoTest() {
        ShortBookingDto actualBooking = BookingMapper.toShortBookingDto(lastBooking);
        assertEquals(actualBooking, lastBookingShort);
    }

    @Test
    public void toBookingResponseDtoList() {
        List<BookingResponseDto> actualList = BookingMapper.toBookingResponseDtoList(List.of(lastBooking, nextBooking));
        assertNotNull(actualList);
        assertEquals(actualList.size(), 2);
        assertEquals(actualList.get(0), lastBookingResponseDto);
        assertEquals(actualList.get(1), nextBookingResponseDto);
    }
}