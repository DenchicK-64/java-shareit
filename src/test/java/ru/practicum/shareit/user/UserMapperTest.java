package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserMapperTest {
    User testUser = new User(1L, "Test_User", "mail@somemail.ru");
    UserDto testUserDto = new UserDto(1L, "Test_User", "mail@somemail.ru");

    @Test
    public void toUserDtoTest() {
        UserDto actualUser = UserMapper.toUserDto(testUser);
        assertEquals(actualUser, testUserDto);
    }

    @Test
    public void toUserTest() {
        User actualUser = UserMapper.toUser(testUserDto);
        assertEquals(actualUser, testUser);
    }
}