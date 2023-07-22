package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.NotAvailableException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    BookingService bookingService;
    @Autowired
    private MockMvc mvc;
    UserDto testUser;
    UserDto testUserTwo;
    ItemDto testItem;
    ItemDto testItemTwo;
    BookingRequestDto testBookingRequestDto;
    BookingRequestDto testBookingRequestDtoTwo;
    BookingResponseDto testBookingResponseDto;
    BookingResponseDto testBookingResponseDtoTwo;

    @BeforeEach
    public void setUp() {
        /*now = LocalDateTime.of(
                2023,
                7,
                10,
                10,
                10,
                10);*/
        testUser = new UserDto(1L, "Test_User", "mail@somemail.ru");
        testUserTwo = new UserDto(2L, "Test_User 2", "user@somemail.ru");
        testItem = new ItemDto(1L, "Test_Name", "Test_Description", true, null);
        testItemTwo = new ItemDto(2L, "Вещь", "Хорошая", true, null);
        testBookingRequestDto = new BookingRequestDto(1L, 1L, LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(11), null);
        testBookingResponseDto = new BookingResponseDto(1L, testBookingRequestDto.getStart(),
                testBookingRequestDto.getEnd(), testItem, testUserTwo, Status.WAITING);
        testBookingRequestDtoTwo = new BookingRequestDto(2L, 2L, LocalDateTime.now().plusDays(12),
                LocalDateTime.now().plusDays(14), null);
        testBookingResponseDtoTwo = new BookingResponseDto(testBookingRequestDtoTwo.getId(), testBookingRequestDtoTwo.getStart(),
                testBookingRequestDtoTwo.getEnd(),
                testItemTwo, testUser, Status.WAITING);
    }

    @Test
    public void create_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        when(bookingService.create(anyLong(), any()))
                .thenReturn(testBookingResponseDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "2")
                        .content(mapper.writeValueAsString(testBookingRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testBookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(testBookingResponseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(testBookingResponseDto.getStatus().toString()), String.class));
    }

    @Test
    public void create_whenInvokedWithoutXSharerHeader_thenReturnStatusBadRequest() throws Exception {
        BookingRequestDto bookingRequestDtoNotValid = new BookingRequestDto(1L, 1L,
                LocalDateTime.now().plusDays(15), LocalDateTime.now().plusDays(16), null);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingRequestDtoNotValid))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_whenInvokedUserNotFound_thenReturnStatusBadRequest() {
        when(bookingService.create(10L, testBookingRequestDto))
                .thenThrow(new NotFoundException("Пользователь с id" + 10 + "не найден в базе данных"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.create(10L, testBookingRequestDto));

        Assertions.assertEquals("Пользователь с id" + 10 + "не найден в базе данных", exception.getMessage());
    }

    @Test
    public void create_whenInvokedWithItemNull_thenReturnStatusBadRequest() throws Exception {
        BookingRequestDto bookingRequestDtoNotValid = new BookingRequestDto(1L, null,
                LocalDateTime.now().plusDays(15), LocalDateTime.now().plusDays(16), null);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingRequestDtoNotValid))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_whenInvokedWithStartTimeNull_thenReturnStatusBadRequest() throws Exception {
        BookingRequestDto bookingRequestDtoNotValid = new BookingRequestDto(1L, 1L,
                null, LocalDateTime.now().plusDays(15), null);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingRequestDtoNotValid))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_whenInvokedWithEndTimeNull_thenReturnStatusBadRequest() throws Exception {
        BookingRequestDto bookingRequestDtoNotValid = new BookingRequestDto(1L, 1L,
                LocalDateTime.now().plusDays(12), null, null);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingRequestDtoNotValid))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_whenInvokedWithTimeInPast_thenReturnStatusBadRequest() throws Exception {
        BookingRequestDto bookingRequestDtoNotValid = new BookingRequestDto(1L, 1L,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), null);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingRequestDtoNotValid))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void approve_whenInvokedWithCorrectDataAndStatusApproved_thenReturnStatusOk() throws Exception {
        testBookingResponseDto.setStatus(Status.APPROVED);
        when(bookingService.approve(1L, 1L, true))
                .thenReturn(testBookingResponseDto);

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", "1")
                        .param("approved", "true")
                        .content(mapper.writeValueAsString(testBookingResponseDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testBookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(testBookingResponseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(testBookingResponseDto.getStatus().toString()), String.class));
    }

    @Test
    public void approve_whenInvokedWithCorrectDataAndStatusRejected_thenReturnStatusOk() throws Exception {
        testBookingResponseDto.setStatus(Status.REJECTED);
        when(bookingService.approve(1L, 1L, false))
                .thenReturn(testBookingResponseDto);

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", "1")
                        .param("approved", "false")
                        .content(mapper.writeValueAsString(testBookingResponseDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testBookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(testBookingResponseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(testBookingResponseDto.getStatus().toString()), String.class));
    }

    @Test
    public void approve_whenInvokedWithNotStatusApproved_thenReturnStatusNotAvailable() {
        testBookingResponseDto.setStatus(Status.APPROVED);
        System.out.println(testBookingResponseDto);
        when(bookingService.approve(1L, 1L, true))
                .thenThrow(new NotAvailableException("Решение по данному запросу на бронирование уже принято"));

        final NotAvailableException exception = Assertions.assertThrows(
                NotAvailableException.class,
                () -> bookingService.approve(1L, 1L, true));

        Assertions.assertEquals("Решение по данному запросу на бронирование уже принято", exception.getMessage());
    }

    @Test
    public void approve_whenInvokedBookingNotFound_thenReturnStatusBNotFound() {
        when(bookingService.approve(1L, 10L, true))
                .thenThrow(new NotFoundException("Запрос на бронирование с id " + 10 + " не найден в базе данных"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.approve(1L, 10L, true));

        Assertions.assertEquals("Запрос на бронирование с id " + 10 + " не найден в базе данных", exception.getMessage());
    }

    @Test
    public void getBooking_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        when(bookingService.getBooking(1L, 1L))
                .thenReturn(testBookingResponseDto);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(testBookingResponseDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testBookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(testBookingResponseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(testBookingResponseDto.getStatus().toString()), String.class));
    }

    @Test
    public void getBooking_whenInvokedWithUserIsNotFound_thenReturnStatusNotFound() {
        when(bookingService.getBooking(10L, 1L))
                .thenThrow(new NotFoundException("Пользователь с id" + 10 + "не найден в базе данных"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getBooking(10L, 1L));

        Assertions.assertEquals("Пользователь с id" + 10 + "не найден в базе данных", exception.getMessage());
    }

    @Test
    public void findAllByBookerId_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        when(bookingService.findAllByBookerId(1L, "ALL", 0, 2))
                .thenReturn(List.of(testBookingResponseDtoTwo));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "2")
                        .content(mapper.writeValueAsString(List.of(testBookingResponseDtoTwo)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(testBookingResponseDtoTwo.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(testBookingResponseDtoTwo.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(testBookingResponseDtoTwo.getStatus().toString()), String.class));
    }

    @Test
    public void findAllByBookerId_whenInvokedWithUserWithoutBookings_thenReturnStatusNotFound() {
        UserDto userDto = new UserDto(3L, "NAME", "mail@mail.ru");
        when(bookingService.findAllByBookerId(3L, "ALL", 0, 2))
                .thenThrow(new NotFoundException("Пользователь " + 3 + " ещё не совершал операций по бронированию вещей"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.findAllByBookerId(3L, "ALL", 0, 2));

        Assertions.assertEquals("Пользователь " + 3 + " ещё не совершал операций по бронированию вещей", exception.getMessage());
    }

    @Test
    public void findAllByBookerId_whenInvokedWithUserIsNotFound_thenReturnStatusNotFound() {
        when(bookingService.findAllByBookerId(1L, "ALL", 0, 2))
                .thenThrow(new NotFoundException("Пользователь с id" + 10 + "не найден в базе данных"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.findAllByBookerId(1L, "ALL", 0, 2));

        Assertions.assertEquals("Пользователь с id" + 10 + "не найден в базе данных", exception.getMessage());
    }

    @Test
    public void findAllByOwnerId_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        when(bookingService.findAllByBookerId(2L, "ALL", 0, 2))
                .thenReturn(List.of(testBookingResponseDto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "2")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "2")
                        .content(mapper.writeValueAsString(List.of(testBookingResponseDto)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(testBookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(testBookingResponseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(testBookingResponseDto.getStatus().toString()), String.class));
    }

    @Test
    public void findAllByOwnerId_whenInvokedWithUserWithoutBookings_thenReturnStatusNotFound() {
        UserDto userDto = new UserDto(3L, "NAME", "mail@mail.ru");
        when(bookingService.findAllByOwnerId(3L, "ALL", 0, 2))
                .thenThrow(new NotFoundException("Отсутствуют операции по бронированию вещей пользователя " + 3));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.findAllByOwnerId(3L, "ALL", 0, 2));

        Assertions.assertEquals("Отсутствуют операции по бронированию вещей пользователя " + 3, exception.getMessage());
    }

    @Test
    public void findAllByOwnerId_whenInvokedWithUserIsNotFound_thenReturnStatusNotFound() {
        when(bookingService.findAllByOwnerId(1L, "ALL", 0, 2))
                .thenThrow(new NotFoundException("Пользователь с id" + 10 + "не найден в базе данных"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.findAllByOwnerId(1L, "ALL", 0, 2));

        Assertions.assertEquals("Пользователь с id" + 10 + "не найден в базе данных", exception.getMessage());
    }
}