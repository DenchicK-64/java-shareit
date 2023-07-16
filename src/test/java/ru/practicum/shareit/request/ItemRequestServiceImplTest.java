package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemRequestServiceImplTest {
    @InjectMocks
    ItemRequestServiceImpl itemRequestService;
    @Mock
    ItemRequestRepository itemRequestRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
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
    private LocalDateTime now;
    User testUser;
    User testUserTwo;
    Item testItem;
    Item testItemTwo;
    ItemRequest itemRequestOne;
    ItemRequest itemRequestTwo;
    ItemRequestDtoWithItems itemRequestDtoWithItemsOne;
    ItemRequestDtoWithItems itemRequestDtoWithItemsTwo;

    @BeforeEach
    public void setUp() {
        testUser = new User(1L, "Test_User", "mail@somemail.ru");
        testUserTwo = new User(2L, "Test_User 2", "user@somemail.ru");
        testItem = new Item(1L, "Test_Name", "Test_Description", true, testUser, null);
        testItemTwo = new Item(2L, "Вещь", "Хорошая", true, testUserTwo, null);
        now = LocalDateTime.of(
                2023,
                7,
                10,
                10,
                10,
                10);
        itemRequestOne = new ItemRequest(1L, "Test_Description", testUserTwo, now);
        itemRequestTwo = new ItemRequest(2L, "Test_Description", testUser, now.plusDays(2));
        itemRequestDtoWithItemsOne = ItemRequestMapper.toItemRequestDtoWithItems(itemRequestOne, List.of(ItemMapper.toItemDto(testItem)));
        itemRequestDtoWithItemsOne = ItemRequestMapper.toItemRequestDtoWithItems(itemRequestTwo, List.of(ItemMapper.toItemDto(testItemTwo)));

    }

    @Test
    public void create_whenDataIsCorrect_thenSaveItem() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(itemRequestRepository.save(any())).thenReturn(itemRequestOne);
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequestOne);
        ItemRequestDto newItemRequestDto = itemRequestService.create(testUserTwo.getId(), itemRequestDto);

        assertNotNull(newItemRequestDto);
        assertEquals(newItemRequestDto.getId(), itemRequestOne.getId());
        assertEquals(newItemRequestDto.getDescription(), itemRequestOne.getDescription());
        assertEquals(newItemRequestDto.getRequester(), UserMapper.toUserDto(testUserTwo));
        assertEquals(newItemRequestDto.getCreated(), itemRequestOne.getCreated());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).save(any());
    }

    @Test
    public void create_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 10;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.create(userId, ItemRequestMapper.toItemRequestDto(itemRequestOne)));
        assertEquals("Пользователь с id " + userId + " не найден в базе данных", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void findAllByUserId_whenDataIsCorrect_thenReturnListItemRequestDtoWithItems() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(anyLong())).thenReturn(List.of(itemRequestOne));
        when(itemRepository.findAllByItemRequest(any())).thenReturn(List.of(testItem));
        List<ItemRequestDtoWithItems> requestDtoWithItemsList = itemRequestService.findAllByUserId(testUserTwo.getId());

        assertNotNull(requestDtoWithItemsList);
        assertEquals(requestDtoWithItemsList.size(), 1);
        assertEquals(requestDtoWithItemsList.get(0).getId(), itemRequestOne.getId());
        assertEquals(requestDtoWithItemsList.get(0).getDescription(), itemRequestOne.getDescription());
        assertEquals(requestDtoWithItemsList.get(0).getRequester(), UserMapper.toUserDto(testUserTwo));
        assertEquals(requestDtoWithItemsList.get(0).getCreated(), itemRequestOne.getCreated());
        assertEquals(requestDtoWithItemsList.get(0).getItems(), List.of(ItemMapper.toItemDto(testItem)));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findAllByRequesterIdOrderByCreatedDesc(anyLong());
        verify(itemRepository, times(1)).findAllByItemRequest(any());
    }

    @Test
    public void findAllByUserId_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 10;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.findAllByUserId(userId));
        assertEquals("Пользователь с id " + userId + " не найден в базе данных", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void findAll_whenDataIsCorrect_thenReturnListItemRequestDtoWithItems() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(itemRequestRepository.findAllByRequesterIdIsNot(anyLong(), any())).thenReturn(List.of(itemRequestTwo));
        when(itemRepository.findAllByItemRequest(any())).thenReturn(List.of(testItemTwo));
        List<ItemRequestDtoWithItems> requestDtoWithItemsList = itemRequestService.findAll(testUserTwo.getId(), 0, 5);

        assertNotNull(requestDtoWithItemsList);
        assertEquals(requestDtoWithItemsList.size(), 1);
        assertEquals(requestDtoWithItemsList.get(0).getId(), itemRequestTwo.getId());
        assertEquals(requestDtoWithItemsList.get(0).getDescription(), itemRequestTwo.getDescription());
        assertEquals(requestDtoWithItemsList.get(0).getRequester(), UserMapper.toUserDto(testUser));
        assertEquals(requestDtoWithItemsList.get(0).getCreated(), itemRequestTwo.getCreated());
        assertEquals(requestDtoWithItemsList.get(0).getItems(), List.of(ItemMapper.toItemDto(testItemTwo)));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findAllByRequesterIdIsNot(anyLong(), any());
        verify(itemRepository, times(1)).findAllByItemRequest(any());
    }

    @Test
    public void findAll_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 10;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.findAll(userId, 0, 5));
        assertEquals("Пользователь с id " + userId + " не найден в базе данных", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void getItemRequestById_whenDataIsCorrect_thenReturnItemRequest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.ofNullable(itemRequestOne));
        when(itemRepository.findAllByItemRequest(any())).thenReturn(List.of(testItem));
        ItemRequestDtoWithItems itemRequestDtoWithItems = itemRequestService.getItemRequestById(testUserTwo.getId(), itemRequestOne.getId());

        assertNotNull(itemRequestDtoWithItems);
        assertEquals(itemRequestDtoWithItems.getId(), itemRequestOne.getId());
        assertEquals(itemRequestDtoWithItems.getDescription(), itemRequestOne.getDescription());
        assertEquals(itemRequestDtoWithItems.getRequester(), UserMapper.toUserDto(testUserTwo));
        assertEquals(itemRequestDtoWithItems.getCreated(), itemRequestOne.getCreated());
        assertEquals(itemRequestDtoWithItems.getItems(), List.of(ItemMapper.toItemDto(testItem)));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByItemRequest(any());
    }

    @Test
    public void getItemRequestById_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 10;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getItemRequestById(userId, itemRequestOne.getId()));
        assertEquals("Пользователь с id " + userId + " не найден в базе данных", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void getItemRequestById_whenItemRequestNotFound_thenNotFoundExceptionThrown() {
        long requestId = 10;
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUserTwo));
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getItemRequestById(testUserTwo.getId(), requestId));
        assertEquals("Запрос с id " + requestId + " не найден в базе данных", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
    }
}