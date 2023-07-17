package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserServiceImplTest {
    @InjectMocks
    UserServiceImpl userService;
    @Mock
    UserRepository userRepository;
    User testUser;
    User testUserTwo;

    @BeforeEach
    public void setUp() {
        testUser = new User(1L, "Test_User", "mail@somemail.ru");
        testUserTwo = new User(2L, "Test_User 2", "user@somemail.ru");
    }

    @Test
    public void create_whenDataIsCorrect_thenSaveUser() {
        when(userRepository.save(any())).thenReturn(testUser);
        UserDto userDto = UserMapper.toUserDto(testUser);
        UserDto newUserDto = userService.create(userDto);

        assertNotNull(newUserDto);
        assertEquals(userDto.getId(), newUserDto.getId());
        assertEquals(userDto.getName(), newUserDto.getName());
        assertEquals(userDto.getEmail(), newUserDto.getEmail());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    public void update_whenDataIsCorrect_thenUpdateUser() {
        UserDto updTestUserDto = new UserDto(1L, "UPDATE", "update@mail.ru");
        User updUser = UserMapper.toUser(updTestUserDto);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(updUser);

        UserDto actualUser = userService.update(1L, updTestUserDto);
        assertNotNull(actualUser);
        assertEquals(actualUser.getId(), updUser.getId());
        assertEquals(actualUser.getId(), updUser.getId());
        assertEquals(actualUser.getEmail(), updUser.getEmail());
        verify(userRepository, times(1)).save(updUser);
    }

    @Test
    public void update_whenUpdateOnlyName_thenUpdateUser() {
        String name = testUser.getName();
        UserDto updTestUserDto = new UserDto(1L, "UPDATE", "mail@somemail.ru");
        User updUser = UserMapper.toUser(updTestUserDto);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(updUser);

        UserDto actualUser = userService.update(1L, updTestUserDto);
        assertNotNull(actualUser);
        assertEquals(actualUser.getId(), updUser.getId());
        assertEquals(actualUser.getId(), updUser.getId());
        assertEquals(actualUser.getEmail(), updUser.getEmail());
        assertNotEquals(name, actualUser.getName());
        verify(userRepository, times(1)).save(updUser);
    }

    @Test
    public void update_whenUpdateOnlyEmail_thenUpdateUser() {
        String email = testUser.getEmail();
        UserDto updTestUserDto = new UserDto(1L, "Test_User", "qwerty@somemail.ru");
        User updUser = UserMapper.toUser(updTestUserDto);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(updUser);

        UserDto actualUser = userService.update(1L, updTestUserDto);
        assertNotNull(actualUser);
        assertEquals(actualUser.getId(), updUser.getId());
        assertEquals(actualUser.getId(), updUser.getId());
        assertEquals(actualUser.getEmail(), updUser.getEmail());
        assertNotEquals(email, actualUser.getEmail());
        verify(userRepository, times(1)).save(updUser);
    }

    @Test
    public void findAll_whenDataIsCorrect_thenReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser, testUserTwo));
        List<UserDto> userDtoList = userService.findAll();

        assertNotNull(userDtoList);
        assertEquals(userDtoList.size(), 2);
        assertEquals(userDtoList.get(0).getId(), testUser.getId());
        assertEquals(userDtoList.get(0).getName(), testUser.getName());
        assertEquals(userDtoList.get(0).getEmail(), testUser.getEmail());
        assertEquals(userDtoList.get(1).getId(), testUserTwo.getId());
        assertEquals(userDtoList.get(1).getName(), testUserTwo.getName());
        assertEquals(userDtoList.get(1).getEmail(), testUserTwo.getEmail());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void findAll_whenUserListEmpty_thenReturnEmptyUserList() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        List<UserDto> userDtoList = userService.findAll();

        assertNotNull(userDtoList);
        assertEquals(userDtoList.size(), 0);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void getUserById_whenDataIsCorrect_thenReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(testUser));
        UserDto userDto = userService.getUser(1L);

        assertNotNull(userDto);
        assertEquals(userDto.getId(), testUser.getId());
        assertEquals(userDto.getName(), testUser.getName());
        assertEquals(userDto.getEmail(), testUser.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void getUserById_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 10;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUser(userId));
        assertEquals("Пользователь с id" + userId + "не найден в базе данных", exception.getMessage());
        verify(userRepository, times(1)).findById(10L);
    }

    @Test
    public void delete_whenDataIsCorrect_thenDeleteUser() {
        userService.delete(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
}