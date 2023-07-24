package ru.practicum.shareit.request;

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
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemRequestControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemRequestService itemRequestService;
    @Autowired
    private MockMvc mvc;
    UserDto testUserDto;
    UserDto testUserTwoDto;
    ItemDto testItemDto;
    ItemDto testItemTwoDto;
    ItemRequestDto testItemRequestDto;
    ItemRequestDtoWithItems testItemRequestDtoWithItems;
    ItemRequestDtoWithItems testItemRequestDtoWithItemsTwo;

    @BeforeEach
    public void setUp() {
        testUserDto = new UserDto(1L, "Test_User", "mail@somemail.ru");
        testUserTwoDto = new UserDto(2L, "Test_User 2", "user@somemail.ru");
        testItemDto = new ItemDto(1L, "Test_Name", "Test_Description", true, 1L);
        testItemTwoDto = new ItemDto(2L, "Вещь", "Хорошая", true, 2L);
        testItemRequestDto = new ItemRequestDto(1L, "Test_Description", testUserDto, LocalDateTime.now().minusDays(5));
        testItemRequestDtoWithItems = new ItemRequestDtoWithItems(2L, "Test_Description_2",
                testUserTwoDto, LocalDateTime.now().minusDays(3), new ArrayList<>());
        testItemRequestDtoWithItemsTwo = new ItemRequestDtoWithItems(3L, "Test_Description_3",
                testUserDto, LocalDateTime.now().minusDays(2), new ArrayList<>());
    }

    @Test
    public void create_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        when(itemRequestService.create(anyLong(), any()))
                .thenReturn(testItemRequestDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(testItemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testItemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(testItemRequestDto.getDescription()), String.class))
                .andExpect(jsonPath("$.requester", is(testItemRequestDto.getRequester()), UserDto.class));
    }

    @Test
    public void create_whenInvokedWithoutXSharerHeader_thenReturnStatusBadRequest() throws Exception {

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(testItemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_whenInvokedUserNotFound_thenReturnStatusNotFound() {
        when(itemRequestService.create(10L, testItemRequestDto))
                .thenThrow(new NotFoundException("Пользователь с id" + 10 + "не найден в базе данных"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemRequestService.create(10L, testItemRequestDto));

        Assertions.assertEquals("Пользователь с id" + 10 + "не найден в базе данных", exception.getMessage());
    }

    @Test
    public void findAll_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        when(itemRequestService.findAll(1L, 0, 2))
                .thenReturn(List.of(testItemRequestDtoWithItems));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "1")
                        .param("from", "0")
                        .param("size", "2")
                        .content(mapper.writeValueAsString(List.of(testItemRequestDtoWithItems)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(testItemRequestDtoWithItems.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(testItemRequestDtoWithItems.getDescription()), String.class))
                .andExpect(jsonPath("$[0].requester", is(testItemRequestDtoWithItems.getRequester()), UserDto.class));
    }

    @Test
    public void findAll_whenInvokedWithUserIsNotFound_thenReturnStatusNotFound() {
        when(itemRequestService.findAll(10L, 0, 2))
                .thenThrow(new NotFoundException("Пользователь с id" + 10 + "не найден в базе данных"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemRequestService.findAll(10L, 0, 2));

        Assertions.assertEquals("Пользователь с id" + 10 + "не найден в базе данных", exception.getMessage());
    }

    @Test
    public void getItemRequestById_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        when(itemRequestService.getItemRequestById(1L, 3L))
                .thenReturn(testItemRequestDtoWithItemsTwo);

        mvc.perform(get("/requests/3")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(testItemRequestDtoWithItemsTwo))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testItemRequestDtoWithItemsTwo.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(testItemRequestDtoWithItemsTwo.getDescription()), String.class))
                .andExpect(jsonPath("$.requester", is(testItemRequestDtoWithItemsTwo.getRequester()), UserDto.class));
    }

    @Test
    public void getItemRequestById_whenInvokedWithUserIsNotFound_thenReturnStatusNotFound() {
        when(itemRequestService.getItemRequestById(10L, 2L))
                .thenThrow(new NotFoundException(("Вещь с id" + 10 + "не найдена в базе данных")));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getItemRequestById(10L, 2L));

        Assertions.assertEquals("Вещь с id" + 10 + "не найдена в базе данных", exception.getMessage());
    }

    @Test
    public void getItemRequestById_whenInvokedWithRequestIsNotFound_thenReturnStatusNotFound() {
        when(itemRequestService.getItemRequestById(1L, 20L))
                .thenThrow(new NotFoundException(("Запрос с id " + 20 + " не найден в базе данных")));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getItemRequestById(1L, 20L));

        Assertions.assertEquals("Запрос с id " + 20 + " не найден в базе данных", exception.getMessage());
    }
}