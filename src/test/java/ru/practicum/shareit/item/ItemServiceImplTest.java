package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.OperationAccessException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBooking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemServiceImplTest {
    @InjectMocks
    ItemServiceImpl itemService;
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
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
    Comment commentOne;
    ItemRequest itemRequest;
    Booking lastBooking;
    Booking nextBooking;

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
        commentOne = new Comment(1L, "some text", testItem, testUserTwo,
                now);
        itemRequest = new ItemRequest(1L, "Test_Description", testUserTwo, now);
        lastBooking = new Booking(1L, now.minusMinutes(20),
                now.plusMinutes(20), testItem, testUserTwo, Status.WAITING);
        nextBooking = new Booking(2L, now.plusDays(8),
                now.plusDays(10), testItem, testUserTwo, Status.WAITING);
    }

    @Test
    public void create_whenDataIsCorrect_thenSaveItem() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(itemRepository.save(any())).thenReturn(testItem);
        ItemDto itemDto = ItemMapper.toItemDto(testItem);
        ItemDto newItemDto = itemService.create(testUser.getId(), itemDto);

        assertNotNull(newItemDto);
        assertEquals(itemDto.getId(), newItemDto.getId());
        assertEquals(itemDto.getName(), newItemDto.getName());
        assertEquals(itemDto.getDescription(), newItemDto.getDescription());
        assertEquals(itemDto.getAvailable(), newItemDto.getAvailable());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).save(testItem);
    }

    @Test
    public void update_whenDataIsCorrect_thenUpdateItem() {
        ItemDto updTestItemDto = new ItemDto(1L, "Updated_Name", "Updated description", true, 1L);
        Item updItem = ItemMapper.toItem(updTestItemDto, testUser, itemRequest);
        when(itemRepository.findItemsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(testItem);
        when(itemRepository.save(any())).thenReturn(updItem);

        ItemDto actualItemDto = itemService.update(1L, 1L, updTestItemDto);
        assertNotNull(actualItemDto);
        assertEquals(actualItemDto.getId(), updItem.getId());
        assertEquals(actualItemDto.getName(), updItem.getName());
        assertEquals(actualItemDto.getDescription(), updItem.getDescription());
        assertEquals(actualItemDto.getAvailable(), updItem.getAvailable());
        verify(itemRepository, times(1)).findItemsByIdAndOwnerId(anyLong(), anyLong());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    public void update_whenUpdateOnlyName_thenUpdateItem() {
        String name = testItem.getName();
        ItemDto updTestItemDto = new ItemDto(1L, "Update", "Test_Description", true, 1L);
        Item updItem = ItemMapper.toItem(updTestItemDto, testUser, itemRequest);
        when(itemRepository.findItemsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(testItem);
        when(itemRepository.save(any())).thenReturn(updItem);

        ItemDto actualItemDto = itemService.update(1L, 1L, updTestItemDto);
        assertNotNull(actualItemDto);
        assertEquals(actualItemDto.getId(), updItem.getId());
        assertEquals(actualItemDto.getName(), updItem.getName());
        assertEquals(actualItemDto.getDescription(), updItem.getDescription());
        assertEquals(actualItemDto.getAvailable(), updItem.getAvailable());
        assertNotEquals(name, actualItemDto.getName());
        verify(itemRepository, times(1)).findItemsByIdAndOwnerId(anyLong(), anyLong());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    public void update_whenUpdateOnlyDescription_thenUpdateItem() {
        String description = testItem.getDescription();
        ItemDto updTestItemDto = new ItemDto(1L, "Test_Name", "Update", true, 1L);
        Item updItem = ItemMapper.toItem(updTestItemDto, testUser, itemRequest);
        when(itemRepository.findItemsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(testItem);
        when(itemRepository.save(any())).thenReturn(updItem);

        ItemDto actualItemDto = itemService.update(1L, 1L, updTestItemDto);
        assertNotNull(actualItemDto);
        assertEquals(actualItemDto.getId(), updItem.getId());
        assertEquals(actualItemDto.getId(), updItem.getId());
        assertEquals(actualItemDto.getDescription(), updItem.getDescription());
        assertEquals(actualItemDto.getAvailable(), updItem.getAvailable());
        assertNotEquals(description, actualItemDto.getDescription());
        verify(itemRepository, times(1)).findItemsByIdAndOwnerId(anyLong(), anyLong());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    public void update_whenUserIsNotOwner_theNotUpdateItem(){
        ItemDto updTestItemDto = new ItemDto(1L, "Not Valid", "Not Valid", true, 1L);
        when(itemRepository.findItemsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(testItem);
        OperationAccessException exception = assertThrows(
                OperationAccessException.class,
                () -> itemService.update(testUserTwo.getId(), 1L, updTestItemDto));
        assertEquals("Нельзя выполнить обновление: пользователь не является собственником вещи", exception.getMessage());
        verify(itemRepository, times(1)).findItemsByIdAndOwnerId(anyLong(), anyLong());
    }

    @Test
    public void findAll_whenDataIsCorrect_thenReturnAllItems() {
        when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
        when(clock.instant()).thenReturn(NOW_ZDT.toInstant());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(itemRepository.findAllByOwnerId(anyLong(), any())).thenReturn(List.of(testItem));
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of(commentOne));
        when(bookingRepository.findAllBookingsItem(anyLong())).thenReturn(List.of(lastBooking, nextBooking));
        List<ItemResponseDtoWithBooking> itemList = itemService.findAll(1L, 0, 5);

        assertNotNull(itemList);
        assertEquals(itemList.size(), 1);
        assertEquals(itemList.get(0).getId(), testItem.getId());
        assertEquals(itemList.get(0).getName(), testItem.getName());
        assertEquals(itemList.get(0).getDescription(), testItem.getDescription());
        assertEquals(itemList.get(0).getLastBooking().getId(), lastBooking.getId());
        assertEquals(itemList.get(0).getNextBooking().getId(), nextBooking.getId());
        assertEquals(itemList.get(0).getComments().get(0).getId(), commentOne.getId());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByOwnerId(anyLong(), any());
        verify(commentRepository, times(1)).findAllByItemId(anyLong());
        verify(bookingRepository, times(1)).findAllBookingsItem(anyLong());
    }

    @Test
    public void findAll_whenItemListEmpty_thenReturnEmptyItemList() {
        User user = new User(3L, "user", "user@user.com");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwnerId(anyLong(), any())).thenReturn(new ArrayList<>());
        List<ItemResponseDtoWithBooking> itemDtoList = itemService.findAll(user.getId(), 0, 2);

        assertNotNull(itemDtoList);
        assertEquals(itemDtoList.size(), 0);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByOwnerId(anyLong(), any());
    }

    @Test
    public void findAll_withNegativeFromParam_ValidationExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.findAll(testUser.getId(), -1, 5));
        assertEquals("Индекс первого элемента не может быть отрицательным и количество отображаемых " +
                "элементов должно быть больше 0", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    public void findAll_withSizeParamIsZero_ValidationExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.findAll(testUser.getId(), 0, 0));
        assertEquals("Индекс первого элемента не может быть отрицательным и количество отображаемых " +
                "элементов должно быть больше 0", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    public void getItem_whenDataIsCorrect_thenReturnItem() {
        when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
        when(clock.instant()).thenReturn(NOW_ZDT.toInstant());
        when(itemRepository.findById(1L)).thenReturn(Optional.ofNullable(testItem));
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of(commentOne));
        when(bookingRepository.findAllBookingsItem(anyLong())).thenReturn(List.of(lastBooking, nextBooking));
        ItemResponseDtoWithBooking item = itemService.getItem(1L, 1L);

        assertNotNull(item);
        assertEquals(item.getId(), testItem.getId());
        assertEquals(item.getName(), testItem.getName());
        assertEquals(item.getDescription(), testItem.getDescription());
        assertEquals(item.getLastBooking().getId(), lastBooking.getId());
        assertEquals(item.getNextBooking().getId(), nextBooking.getId());
        assertEquals(item.getComments().get(0).getId(), commentOne.getId());
        verify(itemRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).findAllByItemId(anyLong());
        verify(bookingRepository, times(1)).findAllBookingsItem(anyLong());
    }

    @Test
    public void getItem_whenItemNotFound_thenNotFoundExceptionThrown() {
        long itemId = 10;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.getItem(testUser.getId(), itemId));
        assertEquals("Вещь с id" + itemId + "не найдена в базе данных", exception.getMessage());
        verify(itemRepository, times(1)).findById(itemId);
    }

    @Test
    public void delete_whenDataIsCorrect_thenDeleteItem() {
        itemService.delete(1L);
        verify(itemRepository, times(1)).deleteById(1L);
    }

    @Test
    public void findItemByName_whenFindByName_thenFindItemList() {
        ItemDto itemDto = ItemMapper.toItemDto(testItemTwo);
        when(itemRepository.search(anyString(), any())).thenReturn(List.of(testItemTwo));
        List<ItemDto> itemDtoList = itemService.findItemByName("Вещь", 0, 5);

        assertNotNull(itemDtoList);
        assertEquals(itemDtoList.size(), 1);
        assertEquals(itemDtoList.get(0).getId(), itemDto.getId());
        assertEquals(itemDtoList.get(0).getName(), itemDto.getName());
        assertEquals(itemDtoList.get(0).getDescription(), itemDto.getDescription());
        assertEquals(itemDtoList.get(0).getAvailable(), itemDto.getAvailable());
        verify(itemRepository, times(1)).search(anyString(), any());
    }

    @Test
    public void findItemByName_whenFindByDescription_thenFindItemList() {
        ItemDto itemDto = ItemMapper.toItemDto(testItemTwo);
        when(itemRepository.search(anyString(), any())).thenReturn(List.of(testItemTwo));
        List<ItemDto> itemDtoList = itemService.findItemByName("Хорошая", 0, 5);

        assertNotNull(itemDtoList);
        assertEquals(itemDtoList.size(), 1);
        assertEquals(itemDtoList.get(0).getId(), itemDto.getId());
        assertEquals(itemDtoList.get(0).getName(), itemDto.getName());
        assertEquals(itemDtoList.get(0).getDescription(), itemDto.getDescription());
        assertEquals(itemDtoList.get(0).getAvailable(), itemDto.getAvailable());
        verify(itemRepository, times(1)).search(anyString(), any());
    }

    @Test
    public void findItemByName_whenItemListEmpty_thenReturnEmptyItemList() {
        when(itemRepository.search(anyString(), any())).thenReturn(new ArrayList<>());
        List<ItemDto> itemDtoList = itemService.findItemByName("текст", 0, 5);

        assertNotNull(itemDtoList);
        assertEquals(itemDtoList.size(), 0);
        verify(itemRepository, times(1)).search(anyString(), any());
    }

    @Test
    public void findItemByName_withNegativeFromParam_ValidationExceptionThrown() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.findItemByName("текст", -1, 5));
        assertEquals("Индекс первого элемента не может быть отрицательным и количество отображаемых " +
                "элементов должно быть больше 0", exception.getMessage());
    }

    @Test
    public void findItemByName_withSizeParamIsZero_ValidationExceptionThrown() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.findItemByName("текст", 0, 0));
        assertEquals("Индекс первого элемента не может быть отрицательным и количество отображаемых " +
                "элементов должно быть больше 0", exception.getMessage());
    }

    @Test
    public void createComment_whenDataIsCorrect_thenAddComment() {
        when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
        when(clock.instant()).thenReturn(NOW_ZDT.toInstant());
        CommentDto commentDto = CommentMapper.toCommentDto(commentOne);
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(testUserTwo));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(testItem));
        when(bookingRepository.findFirstByBookerAndItemAndEndIsBeforeOrderByEndDesc(testUserTwo, testItem, now))
                .thenReturn(lastBooking);
        doReturn(commentOne).when(commentRepository).save(any(Comment.class));
        CommentDto newCommentDto = itemService.createComment(2L, 1L, commentDto);

        assertNotNull(newCommentDto);
        assertEquals(newCommentDto.getId(), commentDto.getId());
        assertEquals(newCommentDto.getText(), commentDto.getText());
        assertEquals(newCommentDto.getItemId(), commentDto.getItemId());
        assertEquals(newCommentDto.getAuthorName(), commentDto.getAuthorName());
        assertEquals(newCommentDto.getCreated(), commentDto.getCreated());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findFirstByBookerAndItemAndEndIsBeforeOrderByEndDesc(testUserTwo, testItem, now);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }
}