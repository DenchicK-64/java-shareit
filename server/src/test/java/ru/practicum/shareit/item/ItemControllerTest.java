package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBooking;

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

@WebMvcTest(controllers = ItemController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemService itemService;
    @Autowired
    private MockMvc mvc;
    UserDto testUserDto;
    UserDto testUserTwoDto;
    ItemDto testItemDto;
    ItemDto testItemTwoDto;
    ItemResponseDtoWithBooking itemResponseDtoWithBooking;
    ItemResponseDtoWithBooking itemResponseDtoWithBookingTwo;
    CommentDto commentOne;

    @BeforeEach
    public void setUp() {
        testUserDto = new UserDto(1L, "Test_User", "mail@somemail.ru");
        testUserTwoDto = new UserDto(2L, "Test_User 2", "user@somemail.ru");
        testItemDto = new ItemDto(1L, "Test_Name", "Test_Description", true, 1L);
        testItemTwoDto = new ItemDto(2L, "Вещь", "Хорошая", true, 2L);
        commentOne = new CommentDto(1L, "some text", 3L, "author1", LocalDateTime.now());
        itemResponseDtoWithBooking = new ItemResponseDtoWithBooking(3L, "Item_With_Booking",
                "Item_With_Booking_Description", true, null, null, new ArrayList<>());
        itemResponseDtoWithBookingTwo = new ItemResponseDtoWithBooking(4L, "Item_With_Booking",
                "Item_With_Booking_Description", true, null, null, new ArrayList<>());
    }

    @Test
    public void create_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        when(itemService.create(anyLong(), any()))
                .thenReturn(testItemDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(testItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(testItemDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(testItemDto.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(testItemDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.requestId", is(testItemDto.getRequestId()), Long.class));
    }

    @Test
    public void create_whenInvokedWithoutXSharerHeader_thenReturnStatusBadRequest() throws Exception {
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(testItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_whenInvokedUserNotFound_thenReturnStatusNotFound() {
        when(itemService.create(10L, testItemDto))
                .thenThrow(new NotFoundException("Пользователь с id" + 10 + "не найден в базе данных"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.create(10L, testItemDto));

        Assertions.assertEquals("Пользователь с id" + 10 + "не найден в базе данных", exception.getMessage());
    }

    @Test
    public void update_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        ItemDto updTestItemDto = new ItemDto(1L, "Update", "New description", true, 1L);

        when(itemService.update(1L, 1L, testItemDto))
                .thenReturn(updTestItemDto);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(testItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updTestItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updTestItemDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(updTestItemDto.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(updTestItemDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.requestId", is(updTestItemDto.getRequestId()), Long.class));
    }

    @Test
    public void update_whenInvokedWithUpdateOnlyName_thenReturnStatusOk() throws Exception {
        ItemDto updTestItemDto = new ItemDto(1L, "Update", "Test_Description", true, 1L);

        when(itemService.update(1L, 1L, testItemDto))
                .thenReturn(updTestItemDto);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(testItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updTestItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updTestItemDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(updTestItemDto.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(updTestItemDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.requestId", is(updTestItemDto.getRequestId()), Long.class));
    }

    @Test
    public void update_whenInvokedWithUpdateOnlyDescription_thenReturnStatusOk() throws Exception {
        ItemDto updTestItemDto = new ItemDto(1L, "Test_Name", "Update", true, 1L);

        when(itemService.update(1L, 1L, testItemDto))
                .thenReturn(updTestItemDto);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(testItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updTestItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updTestItemDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(updTestItemDto.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(updTestItemDto.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.requestId", is(updTestItemDto.getRequestId()), Long.class));
    }

    @Test
    public void update_whenInvokedWithItemIsNotFound_thenReturnStatusNotFound() {
        ItemDto updTestItemDto = new ItemDto(1L, "Test_Name", "Update", true, 1L);

        when(itemService.update(1L, 10L, updTestItemDto))
                .thenThrow(new NotFoundException(("Вещь с id" + 10 + "не найдена в базе данных")));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.update(1L, 10L, updTestItemDto));

        Assertions.assertEquals("Вещь с id" + 10 + "не найдена в базе данных", exception.getMessage());
    }

    @Test
    public void update_whenInvokedWithoutXSharerHeader_thenReturnStatusBadRequest() throws Exception {
        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(testItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void update_whenInvokedUserNotFound_thenReturnStatusNotFound() {
        when(itemService.update(10L, 1L, testItemDto))
                .thenThrow(new NotFoundException("Пользователь с id" + 10 + "не найден в базе данных"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.update(10L, 1L, testItemDto));

        Assertions.assertEquals("Пользователь с id" + 10 + "не найден в базе данных", exception.getMessage());
    }

    @Test
    public void findAll_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        when(itemService.findAll(1L, 0, 2))
                .thenReturn(List.of(itemResponseDtoWithBooking, itemResponseDtoWithBookingTwo));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1")
                        .param("from", "0")
                        .param("size", "2")
                        .content(mapper.writeValueAsString(List.of(itemResponseDtoWithBooking, itemResponseDtoWithBookingTwo)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].id", is(itemResponseDtoWithBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemResponseDtoWithBooking.getName()), String.class))
                .andExpect(jsonPath("$[0].description", is(itemResponseDtoWithBooking.getDescription()), String.class))
                .andExpect(jsonPath("$[0].available", is(itemResponseDtoWithBooking.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$[1].id", is(itemResponseDtoWithBookingTwo.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemResponseDtoWithBookingTwo.getName()), String.class))
                .andExpect(jsonPath("$[1].description", is(itemResponseDtoWithBookingTwo.getDescription()), String.class))
                .andExpect(jsonPath("$[1].available", is(itemResponseDtoWithBookingTwo.getAvailable()), Boolean.class));
    }

    @Test
    public void findAll_whenInvokedWithEmptyUserList_thenReturnStatusOk() throws Exception {
        when(itemService.findAll(1L, 0, 2))
                .thenReturn(new ArrayList<>());

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1")
                        .param("from", "0")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    public void findAll_whenInvokedWithoutXSharerHeader_thenReturnStatusBadRequest() throws Exception {
        when(itemService.findAll(1L, 0, 2))
                .thenReturn(List.of(itemResponseDtoWithBooking, itemResponseDtoWithBookingTwo));

        mvc.perform(get("/items")
                        .param("from", "0")
                        .param("size", "2")
                        .content(mapper.writeValueAsString(List.of(itemResponseDtoWithBooking, itemResponseDtoWithBookingTwo)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void findAll_whenInvokedWithUserIsNotFound_thenReturnStatusNotFound() {
        when(itemService.findAll(10L, 0, 2))
                .thenThrow(new NotFoundException("Пользователь с id" + 10 + "не найден в базе данных"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.findAll(10L, 0, 2));

        Assertions.assertEquals("Пользователь с id" + 10 + "не найден в базе данных", exception.getMessage());
    }

    @Test
    public void getItemById_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        when(itemService.getItem(1L, 3L))
                .thenReturn(itemResponseDtoWithBooking);

        mvc.perform(get("/items/3")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(itemResponseDtoWithBooking))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemResponseDtoWithBooking.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemResponseDtoWithBooking.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemResponseDtoWithBooking.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemResponseDtoWithBooking.getAvailable()), Boolean.class));
    }

    @Test
    public void getItemById_whenInvokedWithItemIsNotFound_thenReturnStatusNotFound() {
        when(itemService.getItem(1L, 10L))
                .thenThrow(new NotFoundException(("Вещь с id" + 10 + "не найдена в базе данных")));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.getItem(1L, 10L));

        Assertions.assertEquals("Вещь с id" + 10 + "не найдена в базе данных", exception.getMessage());
    }

    @Test
    public void delete_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        mvc.perform(delete("/items/1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void findItemByName_whenInvokedWithName_thenReturnStatusOk() throws Exception {
        when(itemService.findItemByName("вещь", 0, 2))
                .thenReturn(List.of(testItemTwoDto));

        mvc.perform(get("/items/search?text=вещь")
                        .param("from", "0")
                        .param("size", "2")
                        .content(mapper.writeValueAsString(List.of(testItemTwoDto)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(testItemTwoDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(testItemTwoDto.getName()), String.class))
                .andExpect(jsonPath("$[0].description", is(testItemTwoDto.getDescription()), String.class))
                .andExpect(jsonPath("$[0].available", is(testItemTwoDto.getAvailable()), Boolean.class));
    }

    @Test
    public void findItemByName_whenInvokedWithDescription_thenReturnStatusOk() throws Exception {
        when(itemService.findItemByName("хорошая", 0, 2))
                .thenReturn(List.of(testItemTwoDto));

        mvc.perform(get("/items/search?text=хорошая")
                        .param("from", "0")
                        .param("size", "2")
                        .content(mapper.writeValueAsString(List.of(testItemTwoDto)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(testItemTwoDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(testItemTwoDto.getName()), String.class))
                .andExpect(jsonPath("$[0].description", is(testItemTwoDto.getDescription()), String.class))
                .andExpect(jsonPath("$[0].available", is(testItemTwoDto.getAvailable()), Boolean.class));
    }

    @Test
    public void createComment_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        when(itemService.createComment(1L, 3L, commentOne))
                .thenReturn(commentOne);

        mvc.perform(post("/items/3/comment")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(commentOne))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentOne.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentOne.getText()), String.class))
                .andExpect(jsonPath("$.itemId", is(commentOne.getItemId()), Long.class))
                .andExpect(jsonPath("$.authorName", is(commentOne.getAuthorName()), String.class));
    }
}