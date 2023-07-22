package ru.practicum.shareit.item.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class CommentRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    User testUser;
    User testUserTwo;
    Item testItem;
    Item testItemTwo;
    Item testItemThree;
    Comment commentOne;

    @BeforeEach
    public void setUp() {
        testUser = userRepository.save(new User(1L, "Test_User", "mail@somemail.ru"));
        testUserTwo = userRepository.save(new User(2L, "Test_User 2", "user@somemail.ru"));
        testItem = itemRepository.save(new Item(1L, "Test_Name", "Test_Description", true, testUser, null));
        testItemTwo = itemRepository.save(new Item(2L, "Вещь", "Хорошая", true, testUserTwo, null));
        testItemThree = itemRepository.save(new Item(13L, "Test_Name3", "Test_Description3", true, testUser, null));
        commentOne = commentRepository.save(new Comment(1L, "some text", testItem, testUserTwo, LocalDateTime.now()));
        commentOne = commentRepository.save(new Comment(2L, "some text2", testItemTwo, testUser, LocalDateTime.now().minusDays(1)));
    }

    @Test
    public void contextLoads() {
        assertNotNull(entityManager);
    }

    @Test
    public void findAllByItemId_whenDataIsCorrect_thenReturnCommentList() {
        List<Comment> comments = commentRepository.findAllByItemId(testItemTwo.getId());
        assertNotNull(comments);
        assertEquals(comments.size(), 1);
        assertEquals(comments.get(0), commentOne);
    }

    @Test
    public void findAllByItemId_whenNotFoundItemsByRequest_thenReturnEmptyList() {
        List<Comment> comments = commentRepository.findAllByItemId(testItemThree.getId());
        assertNotNull(comments);
        assertEquals(comments.size(), 0);
    }
}