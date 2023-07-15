package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    UserService userService;
    @Autowired
    private MockMvc mvc;
    UserDto testUserDto;
    UserDto testUserTwoDto;

    @BeforeEach
    public void setUp() {
        testUserDto = new UserDto(1L, "Test_User", "mail@somemail.ru");
        testUserTwoDto = new UserDto(2L, "Test_User 2", "user@somemail.ru");
    }

    @Test
    public void create_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        when(userService.create(any()))
                .thenReturn(testUserDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(testUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUserDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(testUserDto.getName()), String.class))
                .andExpect(jsonPath("$.email", is(testUserDto.getEmail()), String.class));
    }

    @Test
    public void create_whenInvokedWithEmptyName_thenReturnStatusBadRequest() throws Exception {
        UserDto userDtoNotValid = new UserDto(3L, "", "test@test.ru");

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDtoNotValid))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_whenInvokedWithEmptyEmail_thenReturnStatusBadRequest() throws Exception {
        UserDto userDtoNotValid = new UserDto(3L, "name", "");

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDtoNotValid))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_whenInvokedWithIncorrectEmail_thenReturnStatusBadRequest() throws Exception {
        UserDto userDtoNotValid = new UserDto(3L, "name", "user_mail.ru");

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDtoNotValid))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void update_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        UserDto updUserDto = new UserDto(1L, "update", "update@update.ru");

        when(userService.update(1L, testUserDto))
                .thenReturn(updUserDto);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(testUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updUserDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updUserDto.getName()), String.class))
                .andExpect(jsonPath("$.email", is(updUserDto.getEmail()), String.class));
    }

    @Test
    public void update_whenInvokedWithUpdateOnlyName_thenReturnStatusOk() throws Exception {
        UserDto updUserDto = new UserDto(1L, "update", "mail@somemail.ru");

        when(userService.update(1L, testUserDto))
                .thenReturn(updUserDto);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(testUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updUserDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updUserDto.getName()), String.class))
                .andExpect(jsonPath("$.email", is(updUserDto.getEmail()), String.class));
    }

    @Test
    public void update_whenInvokedWithUpdateOnlyEmail_thenReturnStatusOk() throws Exception {
        UserDto updUserDto = new UserDto(1L, "Test_User", "xxx@somemail.ru");

        when(userService.update(1L, testUserDto))
                .thenReturn(updUserDto);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(testUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updUserDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updUserDto.getName()), String.class))
                .andExpect(jsonPath("$.email", is(updUserDto.getEmail()), String.class));
    }

    @Test
    public void update_whenInvokedWithUserIsNotFound_thenReturnStatusNotFound() {
        UserDto updUserDto = new UserDto(1L, "test", "xxx@somemail.ru");

        when(userService.update(10L, updUserDto))
                .thenThrow(new NotFoundException("Пользователь с id" + 10 + "не найден в базе данных"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> userService.update(10L, updUserDto));

        Assertions.assertEquals("Пользователь с id" + 10 + "не найден в базе данных", exception.getMessage());
    }

    @Test
    public void findAll_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        when(userService.findAll())
                .thenReturn(List.of(testUserDto, testUserTwoDto));

        mvc.perform(get("/users")
                        .content(mapper.writeValueAsString(List.of(testUserDto, testUserTwoDto)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].id", is(testUserDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(testUserDto.getName()), String.class))
                .andExpect(jsonPath("$[0].email", is(testUserDto.getEmail()), String.class))
                .andExpect(jsonPath("$[1].id", is(testUserTwoDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(testUserTwoDto.getName()), String.class))
                .andExpect(jsonPath("$[1].email", is(testUserTwoDto.getEmail()), String.class));
    }

    @Test
    public void findAll_whenInvokedWithEmptyUserList_thenReturnStatusOk() throws Exception {
        when(userService.findAll())
                .thenReturn(new ArrayList<>());

        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    public void getUserById_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        when(userService.getUser(1L))
                .thenReturn(testUserDto);

        mvc.perform(get("/users/1")
                        .content(mapper.writeValueAsString(testUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUserDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(testUserDto.getName()), String.class))
                .andExpect(jsonPath("$.email", is(testUserDto.getEmail()), String.class));
    }

    @Test
    public void getUserById_whenInvokedWithUserIsNotFound_thenReturnStatusNotFound() {
        when(userService.getUser(10L))
                .thenThrow(new NotFoundException("Пользователь с id" + 10 + "не найден в базе данных"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> userService.getUser(10L));

        Assertions.assertEquals("Пользователь с id" + 10 + "не найден в базе данных", exception.getMessage());
    }

    @Test
    public void delete_whenInvokedWithCorrectData_thenReturnStatusOk() throws Exception {
        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}