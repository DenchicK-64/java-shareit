package ru.practicum.shareit.item;

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

import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
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
    public void search_whenDataIsCorrectAndFindByName_thenReturnItemList() {
        String text = "Вещь";
        TypedQuery<Item> query = entityManager.getEntityManager()
                .createQuery(" SELECT i FROM Item i WHERE  (upper(i.name) LIKE upper(concat('%', ?1, '%')) " +
                        "OR upper(i.description) LIKE upper(concat('%', ?1, '%'))) AND i.available = true", Item.class);
        List<Item> items = query.setParameter(1, text).getResultList();
        assertNotNull(items);
        assertEquals(items.size(), 1);
        assertEquals(items.get(0), testItemTwo);

        List<Item> searchResult = itemRepository.search(text, pageRequest);
        assertNotNull(searchResult);
        assertEquals(searchResult.size(), 1);
        assertEquals(searchResult.get(0), testItemTwo);
    }

    @Test
    public void search_whenDataIsCorrectAndFindByDescription_thenReturnItemList() {
        String text = "Хорошая";
        TypedQuery<Item> query = entityManager.getEntityManager()
                .createQuery(" SELECT i FROM Item i WHERE  (upper(i.name) LIKE upper(concat('%', ?1, '%')) " +
                        "OR upper(i.description) LIKE upper(concat('%', ?1, '%'))) AND i.available = true", Item.class);
        List<Item> items = query.setParameter(1, text).getResultList();
        assertNotNull(items);
        assertEquals(items.size(), 1);
        assertEquals(items.get(0), testItemTwo);

        List<Item> searchResult = itemRepository.search(text, pageRequest);
        assertNotNull(searchResult);
        assertEquals(searchResult.size(), 1);
        assertEquals(searchResult.get(0), testItemTwo);
    }

    @Test
    public void search_whenNotFoundItemByNameOrDescription_thenReturnEmptyList() {
        String text = "RANDOM";
        TypedQuery<Item> query = entityManager.getEntityManager()
                .createQuery(" SELECT i FROM Item i WHERE  (upper(i.name) LIKE upper(concat('%', ?1, '%')) " +
                        "OR upper(i.description) LIKE upper(concat('%', ?1, '%'))) AND i.available = true", Item.class);
        List<Item> items = query.setParameter(1, text).getResultList();
        assertNotNull(items);
        assertEquals(items.size(), 0);

        List<Item> searchResult = itemRepository.search(text, pageRequest);
        assertNotNull(searchResult);
        assertEquals(searchResult.size(), 0);
    }

    @Test
    public void findItemsByIdAndOwnerId_whenDataIsCorrect_thenReturnItem() {
        TypedQuery<Item> query = entityManager.getEntityManager()
                .createQuery(" SELECT i FROM Item i WHERE i.id = :itemId AND i.owner.id = :ownerId", Item.class);
        List<Item> items = query.setParameter("itemId", testItemTwo.getId())
                .setParameter("ownerId", testUserTwo.getId()).getResultList();
        assertNotNull(items);
        assertEquals(items.size(), 1);
        assertEquals(items.get(0), testItemTwo);

        Item item = itemRepository.findItemsByIdAndOwnerId(testItemTwo.getId(), testUserTwo.getId());
        assertNotNull(item);
        assertEquals(item, testItemTwo);
    }

    @Test
    public void findAllByOwnerId_whenDataIsCorrect_thenReturnItem() {
        List<Item> items = itemRepository.findAllByOwnerId(testUser.getId(), pageRequest);
        assertNotNull(items);
        assertEquals(items.size(), 1);
        assertEquals(items.get(0), testItem);
    }

    @Test
    public void findAllByOwnerId_whenUserWithoutItems_thenReturnEmptyList() {
        List<Item> items = itemRepository.findAllByOwnerId(testUserThree.getId(), pageRequest);
        assertNotNull(items);
        assertEquals(items.size(), 0);
    }

    @Test
    public void findAllByItemRequest_whenDataIsCorrect_thenReturnItemList() {
        List<Item> items = itemRepository.findAllByItemRequest(itemRequest);
        assertNotNull(items);
        assertEquals(items.size(), 1);
        assertEquals(items.get(0), testItem);
    }

    @Test
    public void findAllByItemRequest_whenNotFoundItemsByRequest_thenReturnEmptyList() {
        List<Item> items = itemRepository.findAllByItemRequest(itemRequestTwo);
        assertNotNull(items);
        assertEquals(items.size(), 0);
    }
}