package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBooking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceIntegrationTest {
    private final ItemService itemService;
    private final UserService userService;
    private static final int DEFAULT_SIZE = 10;

    @SneakyThrows
    @Test
    void findAll() {
        User testUser = createUser("Test_User", "testuser@mail.ru");
        UserDto testUserDto = userService.create(UserMapper.toUserDto(testUser));
        List<Item> itemDtoList = List.of(
                createItem("item1", testUser),
                createItem("item2", testUser),
                createItem("item3", testUser)
        );
        itemDtoList.forEach(item -> itemService.create(testUserDto.getId(), ItemMapper.toItemDto(item)));
        List<ItemResponseDtoWithBooking> items = itemService.findAll(testUserDto.getId(), 0, DEFAULT_SIZE);
        for (ItemResponseDtoWithBooking itemResponseDtoWithBooking : items) {
            assertThat(itemResponseDtoWithBooking.getId(), notNullValue());
            assertThat(itemDtoList, hasItem(hasProperty("name", equalTo(itemResponseDtoWithBooking.getName()))));
        }
    }

    private Item createItem(String name, User owner) {
        Item item = new Item();
        item.setName(name);
        item.setAvailable(true);
        item.setDescription("some text");
        item.setOwner(owner);
        return item;
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}