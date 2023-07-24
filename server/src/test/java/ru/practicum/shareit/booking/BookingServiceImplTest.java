package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exceptions.NotAvailableException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.TimeConflictException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingServiceImplTest {
    @InjectMocks
    BookingServiceImpl bookingService;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    User testUser;
    User testUserTwo;
    Item testItem;
    Item testItemTwo;
    BookingRequestDto testBookingRequestDto;
    BookingRequestDto testBookingRequestDtoTwo;
    Booking testBookingOne;
    Booking testBookingTwo;

    @BeforeEach
    public void setUp() {
        testUser = new User(1L, "Test_User", "mail@somemail.ru");
        testUserTwo = new User(2L, "Test_User 2", "user@somemail.ru");
        testItem = new Item(1L, "Test_Name", "Test_Description", true, testUser, null);
        testItemTwo = new Item(2L, "Вещь", "Хорошая", true, testUserTwo, null);
        testBookingRequestDto = new BookingRequestDto(1L, 1L, LocalDateTime.now().plusMinutes(20),
                LocalDateTime.now().plusMinutes(45), Status.WAITING);
        testBookingRequestDtoTwo = new BookingRequestDto(2L, 2L, LocalDateTime.now().plusDays(8),
                LocalDateTime.now().plusDays(10), Status.WAITING);
        testBookingOne = new Booking(1L, testBookingRequestDto.getStart(), testBookingRequestDto.getEnd(),
                testItem, testUserTwo, Status.WAITING);
        testBookingTwo = new Booking(2L, testBookingRequestDtoTwo.getStart(), testBookingRequestDto.getEnd(),
                testItemTwo, testUser, Status.WAITING);
    }

    @Test
    public void create_whenDataIsCorrect_thenSaveBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(testItem));
        when(bookingRepository.save(any())).thenReturn(testBookingOne);
        BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(testBookingOne);
        BookingResponseDto newBookingResponseDto = bookingService.create(testUserTwo.getId(), testBookingRequestDto);

        assertNotNull(newBookingResponseDto);
        assertEquals(newBookingResponseDto.getId(), bookingResponseDto.getId());
        assertEquals(newBookingResponseDto.getStart(), bookingResponseDto.getStart());
        assertEquals(newBookingResponseDto.getEnd(), bookingResponseDto.getEnd());
        assertEquals(newBookingResponseDto.getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(newBookingResponseDto.getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(newBookingResponseDto.getStatus(), Status.WAITING);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    public void create_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 10;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.create(userId, testBookingRequestDto));
        assertEquals("Пользователь с id " + userId + " не найден в базе данных", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void create_whenUserIsOwner_thenBookingNotSave() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(testItem));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.create(testUser.getId(), testBookingRequestDto));
        assertEquals("Владелец вещи не может её сам забронировать", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    public void create_whenTimeCross_thenBookingNotSave() {
        BookingRequestDto notValidBooking = new BookingRequestDto(3L, 1L, LocalDateTime.now(), LocalDateTime.now().plusMinutes(20),
                Status.WAITING);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(testItem));
        when(bookingRepository.save(any())).thenThrow(new TimeConflictException("Дата окончания бронирования " +
                "не может быть раньше даты начала бронирования или равна ей"));

        TimeConflictException exception = assertThrows(
                TimeConflictException.class,
                () -> bookingService.create(testUserTwo.getId(), notValidBooking));
        assertEquals("Дата окончания бронирования " +
                "не может быть раньше даты начала бронирования или равна ей", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    public void create_whenItemNotAvailable_thenBookingNotSave() {
        testItem.setAvailable(false);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(testItem));

        NotAvailableException exception = assertThrows(
                NotAvailableException.class,
                () -> bookingService.create(testUserTwo.getId(), testBookingRequestDto));
        assertEquals("Вещь с id " + testItem.getId() + " в данный момент не доступна для бронирования",
                exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    public void approve_withApprovedIsTrue_thenApproveBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(testBookingOne));
        when(bookingRepository.save(any())).thenReturn(testBookingOne);
        BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(testBookingOne);
        BookingResponseDto newBookingResponseDto = bookingService.approve(testUser.getId(),
                testBookingRequestDto.getId(), true);

        assertNotNull(newBookingResponseDto);
        assertEquals(newBookingResponseDto.getId(), bookingResponseDto.getId());
        assertEquals(newBookingResponseDto.getStart(), bookingResponseDto.getStart());
        assertEquals(newBookingResponseDto.getEnd(), bookingResponseDto.getEnd());
        assertEquals(newBookingResponseDto.getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(newBookingResponseDto.getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(newBookingResponseDto.getStatus(), Status.APPROVED);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    public void approve_whenApprovedIsFalse_thenRejectBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(testBookingOne));
        when(bookingRepository.save(any())).thenReturn(testBookingOne);
        BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(testBookingOne);
        BookingResponseDto newBookingResponseDto = bookingService.approve(testUser.getId(),
                testBookingRequestDto.getId(), false);

        assertNotNull(newBookingResponseDto);
        assertEquals(newBookingResponseDto.getId(), bookingResponseDto.getId());
        assertEquals(newBookingResponseDto.getStart(), bookingResponseDto.getStart());
        assertEquals(newBookingResponseDto.getEnd(), bookingResponseDto.getEnd());
        assertEquals(newBookingResponseDto.getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(newBookingResponseDto.getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(newBookingResponseDto.getStatus(), Status.REJECTED);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    public void approve_whenBookingNotFound_thenBookingNotBeApproved() {
        long bookingId = 10;
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(bookingRepository.findById(bookingId))
                .thenThrow(new NotFoundException("Запрос на бронирование с id " + bookingId + " не найден в базе данных"));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.approve(testUserTwo.getId(), bookingId, true));
        assertEquals("Запрос на бронирование с id " + bookingId + " не найден в базе данных", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(bookingId);
    }

    @Test
    public void approve_whenBookingWithStartIsBeforeCurrentTime_thenBookingNotBeApproved() {
        testBookingOne.setStart(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(testBookingOne));

        TimeConflictException exception = assertThrows(
                TimeConflictException.class,
                () -> bookingService.approve(testUser.getId(), testBookingRequestDto.getId(), true));
        assertEquals("Время начала брони не может быть раньше текущего", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    public void approve_withApprovedWithoutStatusWaiting_thenApproveNotBeApproved() {
        testBookingOne.setStatus(Status.APPROVED);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(testBookingOne));

        NotAvailableException exception = assertThrows(
                NotAvailableException.class,
                () -> bookingService.approve(testUser.getId(), testBookingRequestDto.getId(), true));
        assertEquals("Решение по данному запросу на бронирование уже принято", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    public void getBooking_whenDataIsCorrect_thenReturnBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(testBookingOne));
        BookingResponseDto bookingResponseDto = bookingService.getBooking(testUserTwo.getId(), testBookingOne.getId());

        assertNotNull(bookingResponseDto);
        assertEquals(bookingResponseDto.getId(), testBookingOne.getId());
        assertEquals(bookingResponseDto.getStart(), testBookingOne.getStart());
        assertEquals(bookingResponseDto.getEnd(), testBookingOne.getEnd());
        assertEquals(bookingResponseDto.getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(bookingResponseDto.getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(bookingResponseDto.getStatus(), Status.WAITING);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    public void getBooking_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 10;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getBooking(userId, testBookingOne.getId()));
        assertEquals("Пользователь с id " + userId + " не найден в базе данных", exception.getMessage());
        verify(userRepository, times(1)).findById(10L);
    }

    @Test
    public void getBooking_whenUserNotOwnerOrBooker_thenNotFoundExceptionThrown() {
        User user = new User(3L, "Random", "random@mail.ru");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getBooking(user.getId(), testBookingOne.getId()));
        assertEquals("Просматривать бронирование может либо владелец вещи, либо автор бронирования",
                exception.getMessage());
        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    public void findAllByBookerId_whenDataIsCorrectAndStateIsAll_thenReturnBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong(), any())).thenReturn(List.of(testBookingOne,
                testBookingTwo));
        List<BookingResponseDto> bookingResponseDtoList = bookingService.findAllByBookerId(testUserTwo.getId(),
                "ALL", 0, 5);

        assertNotNull(bookingResponseDtoList);
        assertEquals(bookingResponseDtoList.size(), 2);
        assertEquals(bookingResponseDtoList.get(0).getId(), testBookingOne.getId());
        assertEquals(bookingResponseDtoList.get(0).getStart(), testBookingOne.getStart());
        assertEquals(bookingResponseDtoList.get(0).getEnd(), testBookingOne.getEnd());
        assertEquals(bookingResponseDtoList.get(0).getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(bookingResponseDtoList.get(0).getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(bookingResponseDtoList.get(0).getStatus(), Status.WAITING);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllByBookerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    public void findAllByBookerId_whenDataIsCorrectAndStateIsCurrent_thenReturnBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(bookingRepository.findAllByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(anyLong(), any(), any(),
                any())).thenReturn(List.of(testBookingOne, testBookingTwo));
        List<BookingResponseDto> bookingResponseDtoList = bookingService.findAllByBookerId(testUserTwo.getId(),
                "CURRENT", 0, 5);

        assertNotNull(bookingResponseDtoList);
        assertEquals(bookingResponseDtoList.size(), 2);
        assertEquals(bookingResponseDtoList.get(0).getId(), testBookingOne.getId());
        assertEquals(bookingResponseDtoList.get(0).getStart(), testBookingOne.getStart());
        assertEquals(bookingResponseDtoList.get(0).getEnd(), testBookingOne.getEnd());
        assertEquals(bookingResponseDtoList.get(0).getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(bookingResponseDtoList.get(0).getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(bookingResponseDtoList.get(0).getStatus(), Status.WAITING);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(anyLong(), any(), any(), any());
    }

    @Test
    public void findAllByBookerId_whenDataIsCorrectAndStateIsPast_thenReturnBooking() {
        testBookingOne.setStart(LocalDateTime.now().minusDays(3));
        testBookingOne.setEnd(LocalDateTime.now().minusDays(1));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(testBookingOne, testBookingTwo));
        List<BookingResponseDto> bookingResponseDtoList = bookingService.findAllByBookerId(testUserTwo.getId(),
                "PAST", 0, 5);

        assertNotNull(bookingResponseDtoList);
        assertEquals(bookingResponseDtoList.size(), 2);
        assertEquals(bookingResponseDtoList.get(0).getId(), testBookingOne.getId());
        assertEquals(bookingResponseDtoList.get(0).getStart(), testBookingOne.getStart());
        assertEquals(bookingResponseDtoList.get(0).getEnd(), testBookingOne.getEnd());
        assertEquals(bookingResponseDtoList.get(0).getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(bookingResponseDtoList.get(0).getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(bookingResponseDtoList.get(0).getStatus(), Status.WAITING);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    public void findAllByBookerId_whenDataIsCorrectAndStateIsFuture_thenReturnBooking() {
        testBookingOne.setStart(LocalDateTime.now().plusDays(1));
        testBookingOne.setEnd(LocalDateTime.now().plusDays(3));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(testBookingOne, testBookingTwo));
        List<BookingResponseDto> bookingResponseDtoList = bookingService.findAllByBookerId(testUserTwo.getId(),
                "FUTURE", 0, 5);

        assertNotNull(bookingResponseDtoList);
        assertEquals(bookingResponseDtoList.size(), 2);
        assertEquals(bookingResponseDtoList.get(0).getId(), testBookingOne.getId());
        assertEquals(bookingResponseDtoList.get(0).getStart(), testBookingOne.getStart());
        assertEquals(bookingResponseDtoList.get(0).getEnd(), testBookingOne.getEnd());
        assertEquals(bookingResponseDtoList.get(0).getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(bookingResponseDtoList.get(0).getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(bookingResponseDtoList.get(0).getStatus(), Status.WAITING);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    public void findAllByBookerId_whenDataIsCorrectAndStatusIsWaiting_thenReturnBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(bookingRepository.findAllByBookerIdAndStartIsAfterAndStatusIsOrderByStartDesc(anyLong(), any(), any(),
                any())).thenReturn(List.of(testBookingOne, testBookingTwo));
        List<BookingResponseDto> bookingResponseDtoList = bookingService.findAllByBookerId(testUserTwo.getId(),
                "WAITING", 0, 5);

        assertNotNull(bookingResponseDtoList);
        assertEquals(bookingResponseDtoList.size(), 2);
        assertEquals(bookingResponseDtoList.get(0).getId(), testBookingOne.getId());
        assertEquals(bookingResponseDtoList.get(0).getStart(), testBookingOne.getStart());
        assertEquals(bookingResponseDtoList.get(0).getEnd(), testBookingOne.getEnd());
        assertEquals(bookingResponseDtoList.get(0).getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(bookingResponseDtoList.get(0).getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(bookingResponseDtoList.get(0).getStatus(), Status.WAITING);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStartIsAfterAndStatusIsOrderByStartDesc(anyLong(), any(), any(), any());
    }

    @Test
    public void findAllByBookerId_whenDataIsCorrectAndStatusIsRejected_thenReturnBooking() {
        testBookingOne.setStatus(Status.REJECTED);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(bookingRepository.findAllByBookerIdAndStatusIsOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(testBookingOne, testBookingTwo));
        List<BookingResponseDto> bookingResponseDtoList = bookingService.findAllByBookerId(testUserTwo.getId(),
                "REJECTED", 0, 5);

        assertNotNull(bookingResponseDtoList);
        assertEquals(bookingResponseDtoList.size(), 2);
        assertEquals(bookingResponseDtoList.get(0).getId(), testBookingOne.getId());
        assertEquals(bookingResponseDtoList.get(0).getStart(), testBookingOne.getStart());
        assertEquals(bookingResponseDtoList.get(0).getEnd(), testBookingOne.getEnd());
        assertEquals(bookingResponseDtoList.get(0).getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(bookingResponseDtoList.get(0).getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(bookingResponseDtoList.get(0).getStatus(), Status.REJECTED);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStatusIsOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    public void findAllByBookerId_withUnknownState_ValidationExceptionThrown() {
        String state = "all";
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.findAllByBookerId(testUser.getId(), state, 0, 5));
        assertEquals("Unknown state: " + state, exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    public void findAllByBookerId_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 10;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.findAllByBookerId(userId, "ALL", 0, 5));
        assertEquals("Пользователь с id " + userId + " не найден в базе данных", exception.getMessage());
        verify(userRepository, times(1)).findById(10L);
    }

    @Test
    public void findAllByOwnerId_whenDataIsCorrectAndStateIsAll_thenReturnBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findAllBookingsByOwnerId(anyLong(), any())).thenReturn(List.of(testBookingOne, testBookingTwo));
        List<BookingResponseDto> bookingResponseDtoList = bookingService.findAllByOwnerId(testUser.getId(),
                "ALL", 0, 5);

        assertNotNull(bookingResponseDtoList);
        assertEquals(bookingResponseDtoList.size(), 2);
        assertEquals(bookingResponseDtoList.get(0).getId(), testBookingOne.getId());
        assertEquals(bookingResponseDtoList.get(0).getStart(), testBookingOne.getStart());
        assertEquals(bookingResponseDtoList.get(0).getEnd(), testBookingOne.getEnd());
        assertEquals(bookingResponseDtoList.get(0).getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(bookingResponseDtoList.get(0).getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(bookingResponseDtoList.get(0).getStatus(), Status.WAITING);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findAllBookingsByOwnerId(anyLong(), any());
    }

    @Test
    public void findAllByOwnerId_whenDataIsCorrectAndStateIsCurrent_thenReturnBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findAllBookingsByOwnerIdWithStatusCurrent(anyLong(), any(), any()))
                .thenReturn(List.of(testBookingOne, testBookingTwo));
        List<BookingResponseDto> bookingResponseDtoList = bookingService.findAllByOwnerId(testUser.getId(),
                "CURRENT", 0, 5);

        assertNotNull(bookingResponseDtoList);
        assertEquals(bookingResponseDtoList.size(), 2);
        assertEquals(bookingResponseDtoList.get(0).getId(), testBookingOne.getId());
        assertEquals(bookingResponseDtoList.get(0).getStart(), testBookingOne.getStart());
        assertEquals(bookingResponseDtoList.get(0).getEnd(), testBookingOne.getEnd());
        assertEquals(bookingResponseDtoList.get(0).getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(bookingResponseDtoList.get(0).getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(bookingResponseDtoList.get(0).getStatus(), Status.WAITING);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllBookingsByOwnerIdWithStatusCurrent(anyLong(), any(), any());
    }

    @Test
    public void findAllByOwnerId_whenDataIsCorrectAndStateIsPast_thenReturnBooking() {
        testBookingOne.setStart(LocalDateTime.now().minusDays(3));
        testBookingOne.setEnd(LocalDateTime.now().minusDays(1));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findAllBookingsByOwnerIdWithStatusPast(anyLong(), any(), any()))
                .thenReturn(List.of(testBookingOne, testBookingTwo));
        List<BookingResponseDto> bookingResponseDtoList = bookingService.findAllByOwnerId(testUser.getId(),
                "PAST", 0, 5);

        assertNotNull(bookingResponseDtoList);
        assertEquals(bookingResponseDtoList.size(), 2);
        assertEquals(bookingResponseDtoList.get(0).getId(), testBookingOne.getId());
        assertEquals(bookingResponseDtoList.get(0).getStart(), testBookingOne.getStart());
        assertEquals(bookingResponseDtoList.get(0).getEnd(), testBookingOne.getEnd());
        assertEquals(bookingResponseDtoList.get(0).getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(bookingResponseDtoList.get(0).getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(bookingResponseDtoList.get(0).getStatus(), Status.WAITING);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllBookingsByOwnerIdWithStatusPast(anyLong(), any(), any());
    }

    @Test
    public void findAllByOwnerId_whenDataIsCorrectAndStateIsFuture_thenReturnBooking() {
        testBookingOne.setStart(LocalDateTime.now().plusDays(1));
        testBookingOne.setEnd(LocalDateTime.now().plusDays(3));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findAllBookingsByOwnerIdWithStatusFuture(anyLong(), any(), any()))
                .thenReturn(List.of(testBookingOne, testBookingTwo));
        List<BookingResponseDto> bookingResponseDtoList = bookingService.findAllByOwnerId(testUser.getId(),
                "FUTURE", 0, 5);

        assertNotNull(bookingResponseDtoList);
        assertEquals(bookingResponseDtoList.size(), 2);
        assertEquals(bookingResponseDtoList.get(0).getId(), testBookingOne.getId());
        assertEquals(bookingResponseDtoList.get(0).getStart(), testBookingOne.getStart());
        assertEquals(bookingResponseDtoList.get(0).getEnd(), testBookingOne.getEnd());
        assertEquals(bookingResponseDtoList.get(0).getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(bookingResponseDtoList.get(0).getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(bookingResponseDtoList.get(0).getStatus(), Status.WAITING);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllBookingsByOwnerIdWithStatusFuture(anyLong(), any(), any());
    }

    @Test
    public void findAllByOwnerId_whenDataIsCorrectAndStatusIsWaiting_thenReturnBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findAllBookingsByOwnerIdWithStatusWaiting(anyLong(), any(), any(), any()))
                .thenReturn(List.of(testBookingOne, testBookingTwo));
        List<BookingResponseDto> bookingResponseDtoList = bookingService.findAllByOwnerId(testUser.getId(),
                "WAITING", 0, 5);

        assertNotNull(bookingResponseDtoList);
        assertEquals(bookingResponseDtoList.size(), 2);
        assertEquals(bookingResponseDtoList.get(0).getId(), testBookingOne.getId());
        assertEquals(bookingResponseDtoList.get(0).getStart(), testBookingOne.getStart());
        assertEquals(bookingResponseDtoList.get(0).getEnd(), testBookingOne.getEnd());
        assertEquals(bookingResponseDtoList.get(0).getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(bookingResponseDtoList.get(0).getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(bookingResponseDtoList.get(0).getStatus(), Status.WAITING);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllBookingsByOwnerIdWithStatusWaiting(anyLong(), any(), any(), any());
    }

    @Test
    public void findAllByOwnerId_whenDataIsCorrectAndStatusIsRejected_thenReturnBooking() {
        testBookingOne.setStatus(Status.REJECTED);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findAllBookingsByOwnerIdWithStatusRejected(anyLong(), any(), any()))
                .thenReturn(List.of(testBookingOne, testBookingTwo));
        List<BookingResponseDto> bookingResponseDtoList = bookingService.findAllByOwnerId(testUser.getId(),
                "REJECTED", 0, 5);

        assertNotNull(bookingResponseDtoList);
        assertEquals(bookingResponseDtoList.size(), 2);
        assertEquals(bookingResponseDtoList.get(0).getId(), testBookingOne.getId());
        assertEquals(bookingResponseDtoList.get(0).getStart(), testBookingOne.getStart());
        assertEquals(bookingResponseDtoList.get(0).getEnd(), testBookingOne.getEnd());
        assertEquals(bookingResponseDtoList.get(0).getItem(), ItemMapper.toItemDto(testItem));
        assertEquals(bookingResponseDtoList.get(0).getBooker(), UserMapper.toUserDto(testUserTwo));
        assertEquals(bookingResponseDtoList.get(0).getStatus(), Status.REJECTED);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllBookingsByOwnerIdWithStatusRejected(anyLong(), any(), any());
    }

    @Test
    public void findAllByOwnerId_withUnknownState_ValidationExceptionThrown() {
        String state = "all";
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.findAllByOwnerId(testUser.getId(), state, 0, 5));
        assertEquals("Unknown state: " + state, exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    public void findAllByOwnerId_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 10;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.findAllByOwnerId(userId, "ALL", 0, 5));
        assertEquals("Пользователь с id " + userId + " не найден в базе данных", exception.getMessage());
        verify(userRepository, times(1)).findById(10L);
    }
}