package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemRequestMapperTest {
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
    UserDto testUserDto = new UserDto(1L, "Test_User", "mail@somemail.ru");

    ItemRequest itemRequest = new ItemRequest(1L, "Test_Description", testUser, now);

    ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "Test_Description", testUserDto, now);
    ItemDto testItemDto = new ItemDto(1L, "Test_Name", "Test_Description", true, null);

    ItemRequestDtoWithItems itemRequestDtoWithItems = new ItemRequestDtoWithItems(1L, "Test_Description", testUserDto,
            now, List.of(testItemDto));

    @Test
    public void toItemRequestDtoTest() {
        ItemRequestDto actualRequest = ItemRequestMapper.toItemRequestDto(itemRequest);
        assertEquals(actualRequest, itemRequestDto);
    }

   @Test
    public void toItemRequest() {
        ItemRequest actualRequest = ItemRequestMapper.toItemRequest(itemRequestDto, testUser);
        assertEquals(actualRequest, itemRequest);
    }

    @Test
    public void toItemRequestDtoWithItems() {
        ItemRequestDtoWithItems actualRequest = ItemRequestMapper.toItemRequestDtoWithItems(itemRequest, List.of(testItemDto));
        assertEquals(actualRequest, itemRequestDtoWithItems);
        }
}