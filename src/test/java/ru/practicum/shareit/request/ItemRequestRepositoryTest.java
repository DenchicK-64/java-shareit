package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class ItemRequestRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    PageRequest pageRequest = PageRequest.of(0, 5);
    User testUser;
    User testUserTwo;
    User testUserThree;
    Item testItem;
    Item testItemTwo;
    ItemRequest itemRequest;
    ItemRequest itemRequestTwo;

    @BeforeEach
    public void setUp() {
        testUser = userRepository.save(new User(1L, "Test_User", "mail@somemail.ru"));
        testUserTwo = userRepository.save(new User(2L, "Test_User 2", "user@somemail.ru"));
        testUserThree = userRepository.save(new User(3L, "Test_User 3", "xxx@somemail.ru"));
        itemRequest = itemRequestRepository.save(new ItemRequest(1L, "Test_Description", testUserTwo,
                LocalDateTime.now().plusHours(3)));
        itemRequestTwo = itemRequestRepository.save(new ItemRequest(2L, "Test_Description2", testUser,
                LocalDateTime.now().minusHours(1)));
        testItem = itemRepository.save(new Item(1L, "Test_Name", "Test_Description", true,
                testUser, itemRequest));
        testItemTwo = itemRepository.save(new Item(2L, "Вещь", "Хорошая", true,
                testUserTwo, null));
    }

    @Test
    public void contextLoads() {
        assertNotNull(entityManager);
    }

    @Test
    public void findAllByRequesterIdOrderByCreatedDesc_whenDataIsCorrect_thenReturnItemRequestList() {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(testUser.getId());
        assertNotNull(requests);
        assertEquals(requests.size(), 1);
        assertEquals(requests.get(0), itemRequestTwo);
    }

    @Test
    public void findAllByRequesterIdOrderByCreatedDesc_whenDataIsCorrect_thenReturnEmptyList() {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(testUserThree.getId());
        assertNotNull(requests);
        assertEquals(requests.size(), 0);
    }

    @Test
    public void findAllByRequesterIdIsNot_whenDataIsCorrect_thenReturnItemRequestList() {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdIsNot(testUser.getId(), pageRequest);
        assertNotNull(requests);
        assertEquals(requests.size(), 1);
        assertEquals(requests.get(0), itemRequest);
    }

    @Test
    public void findAllByRequesterIdIsNot_whenDataIsCorrect_thenReturnEmptyList() {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdIsNot(testUserThree.getId(), pageRequest);
        assertNotNull(requests);
        assertEquals(requests.size(), 2);
        assertEquals(requests, List.of(itemRequest, itemRequestTwo));
    }
}