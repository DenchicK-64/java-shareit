package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class UserDtoTest {
    @Autowired
    private JacksonTester<UserDto> json;
    UserDto testUserDto;

    @BeforeEach
    public void setUp() {
        testUserDto = new UserDto(1L, "Test_User", "mail@somemail.ru");
    }

    @Test
    public void serialize() throws IOException {
        JsonContent<UserDto> userDto = json.write(testUserDto);
        assertThat(userDto).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(userDto).extractingJsonPathStringValue("$.name").isEqualTo("Test_User");
        assertThat(userDto).extractingJsonPathStringValue("$.email").isEqualTo("mail@somemail.ru");
    }

    @Test
    public void deserialize() throws IOException {
        String jsonContent = String.format("{\"id\":\"%s\", \"name\":\"%s\", \"email\": \"%s\"}",
                testUserDto.getId(), testUserDto.getName(), testUserDto.getEmail());
        UserDto userDto = this.json.parse(jsonContent).getObject();
        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getName()).isEqualTo("Test_User");
        assertThat(userDto.getEmail()).isEqualTo("mail@somemail.ru");
    }
}