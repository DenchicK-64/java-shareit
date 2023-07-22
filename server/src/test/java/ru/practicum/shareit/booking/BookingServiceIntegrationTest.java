package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceIntegrationTest {
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;
    private static final int DEFAULT_SIZE = 10;

    @Test
    public void findAllByBookerId() {
        User testUser = createUser("Test_User", "testuser@mail.ru");
        UserDto testUserDto = userService.create(UserMapper.toUserDto(testUser));
        log.info(testUserDto.toString());
        User testUser2 = createUser("Test_USER2", "testUser2@mail.ru");
        UserDto testUserDto2 = userService.create(UserMapper.toUserDto(testUser2));
        log.info(testUserDto2.toString());
        Item testItem = createItem("item1", testUser);
        ItemDto testItemDto = itemService.create(testUserDto.getId(), ItemMapper.toItemDto(testItem));
        log.info(testItemDto.toString());
        Item testItem2 = createItem("item2", testUser2);
        ItemDto testItemDto2 = itemService.create(testUserDto2.getId(), ItemMapper.toItemDto(testItem2));
        log.info(testItemDto2.toString());
        //findAllByOwnerId
        BookingRequestDto bookingRequestDto1 = createBookingRequestDto(testItemDto);
        BookingResponseDto bookingResponseDto1 = bookingService.create(testUserDto2.getId(), bookingRequestDto1);
        log.info(bookingResponseDto1.toString());
        //findAllByBookerId
        BookingRequestDto bookingRequestDto2 = createBookingRequestDto(testItemDto2);
        BookingResponseDto bookingResponseDto2 = bookingService.create(testUserDto.getId(), bookingRequestDto2);
        log.info(bookingResponseDto2.toString());
        List<BookingResponseDto> bookingResponseDtoList = List.of(bookingResponseDto1, bookingResponseDto2);

        Long bookerId = testUserDto2.getId();
        List<BookingResponseDto> bookingResponseDtos = bookingService.findAllByBookerId(bookerId, "ALL", 0, DEFAULT_SIZE);
        assertThat(bookingResponseDtos, notNullValue());
        assertThat(bookingResponseDtos, hasSize(1));
        assertThat(bookingResponseDtos.get(0).getId(), notNullValue());
        assertThat(bookingResponseDtos.get(0).getBooker().getName(), equalTo(bookingResponseDtoList.get(0).getBooker().getName()));
    }

   @Test
    public void findAllByOwnerId() {
        User testUser = createUser("Test_User", "testuser@mail.ru");
        UserDto testUserDto = userService.create(UserMapper.toUserDto(testUser));
        log.info(testUserDto.toString());
        User testUser2 = createUser("Test_USER2", "testUser2@mail.ru");
        UserDto testUserDto2 = userService.create(UserMapper.toUserDto(testUser2));
        log.info(testUserDto2.toString());
        Item testItem = createItem("item1", testUser);
        ItemDto testItemDto = itemService.create(testUserDto.getId(), ItemMapper.toItemDto(testItem));
        log.info(testItemDto.toString());
        Item testItem2 = createItem("item2", testUser2);
        ItemDto testItemDto2 = itemService.create(testUserDto2.getId(), ItemMapper.toItemDto(testItem2));
        log.info(testItemDto2.toString());
        //findAllByOwnerId
        BookingRequestDto bookingRequestDto1 = createBookingRequestDto(testItemDto);
        BookingResponseDto bookingResponseDto1 = bookingService.create(testUserDto2.getId(), bookingRequestDto1);
        log.info(bookingResponseDto1.toString());
        //findAllByBookerId
        BookingRequestDto bookingRequestDto2 = createBookingRequestDto(testItemDto2);
        BookingResponseDto bookingResponseDto2 = bookingService.create(testUserDto.getId(), bookingRequestDto2);
        log.info(bookingResponseDto2.toString());
        List<BookingResponseDto> bookingResponseDtoList = List.of(bookingResponseDto1, bookingResponseDto2);

        Long ownerId = testUserDto2.getId();
        List<BookingResponseDto> bookingResponseDtos = bookingService.findAllByOwnerId(ownerId, "ALL", 0, DEFAULT_SIZE);
        assertThat(bookingResponseDtos, notNullValue());
        assertThat(bookingResponseDtos, hasSize(1));
        assertThat(bookingResponseDtos.get(0).getId(), notNullValue());
        assertThat(bookingResponseDtos.get(0).getBooker().getName(), equalTo(bookingResponseDtoList.get(1).getBooker().getName()));
    }

    private BookingRequestDto createBookingRequestDto(ItemDto itemDto) {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(itemDto.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusHours(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusHours(2));
        bookingRequestDto.setStatus(Status.WAITING);
        return bookingRequestDto;
    }

    private Item createItem(String name, User user) {
        Item item = new Item();
        item.setName(name);
        item.setAvailable(true);
        item.setDescription("some text");
        item.setOwner(user);
        return item;
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}