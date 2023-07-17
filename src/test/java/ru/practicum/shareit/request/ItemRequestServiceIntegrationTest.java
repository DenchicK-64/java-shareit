package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
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
public class ItemRequestServiceIntegrationTest {
    private final ItemRequestService itemRequestService;
    private final UserService userService;
    private static final int DEFAULT_SIZE = 10;

    @Test
    void findAll() {
        User testUser = createUser("Test_User", "testuser@mail.ru");
        UserDto testUserDto = userService.create(UserMapper.toUserDto(testUser));
        User testUser2 = createUser("Test_User2", "testuser2@mail.ru");
        UserDto testUserDto2 = userService.create(UserMapper.toUserDto(testUser2));
        List<ItemRequest> itemRequestList = List.of(
                createItemRequest("ItemRequest 1", testUser),
                createItemRequest("ItemRequest 2", testUser),
                createItemRequest("ItemRequest 3", testUser)
        );
        itemRequestList.forEach(itemRequest -> itemRequestService.create(testUserDto.getId(), ItemRequestMapper.toItemRequestDto(itemRequest)));
        List<ItemRequestDtoWithItems> itemRequests = itemRequestService.findAll(testUserDto2.getId(), 0, DEFAULT_SIZE);
        for (ItemRequestDtoWithItems itemRequestDtoWithItems : itemRequests) {
            assertThat(itemRequestDtoWithItems.getId(), notNullValue());
            assertThat(itemRequestList, hasItem(hasProperty("description", equalTo(itemRequestDtoWithItems.getDescription()))));
        }
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private ItemRequest createItemRequest(String description, User requester) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequester(requester);
        return request;
    }
}