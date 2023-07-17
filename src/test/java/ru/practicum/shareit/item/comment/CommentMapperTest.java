package ru.practicum.shareit.item.comment;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommentMapperTest {
    @Mock
    private Clock clock;
    private static final ZonedDateTime NOW_ZDT = ZonedDateTime.of(
            2023,
            7,
            10,
            10,
            10,
            10,
            0,
            ZoneId.of("UTC")
    );
    private LocalDateTime now = LocalDateTime.of(
            2023,
            7,
            10,
            10,
            10,
            10);
    User testUser = new User(1L, "Test_User", "mail@somemail.ru");
    User testUserTwo = new User(2L, "Test_User2", "mail2@somemail.ru");
    Item testItem = new Item(1L, "Test_Name", "Test_Description", true, testUserTwo, null);
    Item testItemTwo = new Item(2L, "Test_Name2", "Test_Description2", true, testUser, null);
    Comment commentOne = new Comment(1L, "some text", testItem, testUserTwo, now);
    CommentDto commentOneDto = new CommentDto(1L, "some text", testItem.getId(), "Test_User2", now);
    Comment commentTwo = new Comment(2L, "some text", testItemTwo, testUser, now);
    CommentDto commentTwoDto = new CommentDto(2L, "some text", testItemTwo.getId(), "Test_User", now);

    @Test
    public void toCommentDtoTest() {
        CommentDto actualComment = CommentMapper.toCommentDto(commentOne);
        assertEquals(actualComment, commentOneDto);
    }

    @Test
    public void toCommentTest() {
        Comment actualComment = CommentMapper.toComment(commentOneDto, testUserTwo, testItem, now);
        assertEquals(actualComment, commentOne);
    }

    @Test
    public void toCommentDtoListTest() {
        List<CommentDto> actualList = CommentMapper.toCommentDtoList(List.of(commentOne, commentTwo));
        assertNotNull(actualList);
        assertEquals(actualList.size(), 2);
        assertEquals(actualList.get(0), commentOneDto);
        assertEquals(actualList.get(1), commentTwoDto);
    }
}