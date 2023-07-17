package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class ItemDtoTest {
    @Autowired
    private JacksonTester<ItemDto> json;
    ItemDto testItemDto;

    @BeforeEach
    public void setUp() {
        testItemDto = new ItemDto(1L, "Test_Name", "Test_Description", true, null);
    }

    @Test
    public void serialize() throws IOException {
        JsonContent<ItemDto> itemDto = json.write(testItemDto);
        assertThat(itemDto).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(itemDto).extractingJsonPathStringValue("$.name").isEqualTo("Test_Name");
        assertThat(itemDto).extractingJsonPathStringValue("$.description").isEqualTo("Test_Description");
        assertThat(itemDto).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
    }

    @Test
    public void deserialize() throws IOException {
        String jsonContent = String.format("{\"id\":\"%s\", \"name\":\"%s\", \"description\": \"%s\", \"available\": \"%s\"}",
                testItemDto.getId(), testItemDto.getName(), testItemDto.getDescription(), testItemDto.getAvailable());
        ItemDto itemDto = this.json.parse(jsonContent).getObject();
        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Test_Name");
        assertThat(itemDto.getDescription()).isEqualTo("Test_Description");
        assertThat(itemDto.getAvailable()).isEqualTo(true);
    }
}