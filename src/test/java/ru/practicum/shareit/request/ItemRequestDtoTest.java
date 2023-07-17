package ru.practicum.shareit.request;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class ItemRequestDtoTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;
    UserDto testUserDto;
    ItemRequestDto testItemRequestDto;

    @BeforeEach
    public void setUp() {
        testUserDto = new UserDto(1L, "Test_User", "mail@somemail.ru");
        testItemRequestDto = new ItemRequestDto(1L, "Test_Description", testUserDto, LocalDateTime.now());
    }

    @Test
    public void serialize() throws IOException {
        JsonContent<ItemRequestDto> itemRequestDto = json.write(testItemRequestDto);
        assertThat(itemRequestDto).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(itemRequestDto).extractingJsonPathStringValue("$.description").isEqualTo("Test_Description");
    }

    @Test
    public void deserialize() throws IOException {
        String jsonContent = String.format("{\"id\":\"%s\", \"description\":\"%s\"}", testItemRequestDto.getId(),
                testItemRequestDto.getDescription());
        ItemRequestDto itemRequestDto = this.json.parse(jsonContent).getObject();
        AssertionsForClassTypes.assertThat(itemRequestDto.getId()).isEqualTo(1L);
        AssertionsForClassTypes.assertThat(itemRequestDto.getDescription()).isEqualTo("Test_Description");
    }
}