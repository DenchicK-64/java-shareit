package ru.practicum.shareit.item.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.comment.dto.CommentDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class CommentDtoTest {
    @Autowired
    private JacksonTester<CommentDto> json;
    CommentDto commentDto;

    @BeforeEach
    public void setUp() {
        commentDto = new CommentDto(1L, "some text", 1L, "author", LocalDateTime.now());
    }

    @Test
    public void serialize() throws IOException {
        JsonContent<CommentDto> comment = json.write(commentDto);
        assertThat(comment).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(comment).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(comment).extractingJsonPathStringValue("$.text").isEqualTo("some text");
        assertThat(comment).extractingJsonPathStringValue("$.authorName").isEqualTo("author");
    }

    @Test
    public void deserialize() throws IOException {
        String jsonContent = String.format("{\"id\":\"%s\", \"itemId\":\"%s\", \"text\":\"%s\", \"authorName\": \"%s\"}",
                commentDto.getId(), commentDto.getItemId(), commentDto.getText(), commentDto.getAuthorName());
        CommentDto comment = this.json.parse(jsonContent).getObject();
        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getItemId()).isEqualTo(1L);
        assertThat(comment.getText()).isEqualTo("some text");
        assertThat(commentDto.getAuthorName()).isEqualTo("author");
    }
}
