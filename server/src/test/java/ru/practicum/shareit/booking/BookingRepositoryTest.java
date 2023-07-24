package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private BookingRepository bookingRepository;
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
    Booking testBooking;
    Booking testBookingTwo;
    Booking testBookingThree;

    @BeforeEach
    public void setUp() {
        testUser = userRepository.save(new User(1L, "Test_User", "mail@somemail.ru"));
        testUserTwo = userRepository.save(new User(2L, "Test_User 2", "user@somemail.ru"));
        testUserThree = userRepository.save(new User(3L, "Test_User 3", "xxx@somemail.ru"));
        testItem = itemRepository.save(new Item(1L, "Test_Name", "Test_Description", true,
                testUser, null));
        testItemTwo = itemRepository.save(new Item(2L, "Вещь", "Хорошая", true, testUserTwo,
                null));
        testBooking = bookingRepository.save(new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now(),
                testItem, testUserTwo, Status.WAITING));
        testBookingTwo = bookingRepository.save(new Booking(2L, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(2), testItemTwo, testUser, Status.WAITING));
        testBookingThree = bookingRepository.save(new Booking(3L, LocalDateTime.now(),
                LocalDateTime.now().plusDays(5), testItemTwo, testUser, Status.WAITING));
    }

    @Test
    public void contextLoads() {
        assertNotNull(entityManager);
    }

    //Получение всех бронирований пользователя:

    //ALL
    @Test
    public void findAllByBookerIdOrderByStartDesc_whenDataIsCorrectAndStateAll_thenReturnBookingList() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(testUser.getId(), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings, List.of(testBookingThree, testBookingTwo));
    }

    @Test
    public void findAllByBookerIdOrderByStartDesc_whenUserWithoutBookings_thenReturnEmptyList() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(testUserThree.getId(), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 0);
    }

    //WAITING
    @Test
    public void findAllByBookerIdAndStartIsAfterAndStatusIsOrderByStartDesc_whenDataIsCorrectAndStatusWaiting_thenReturnBookingList() {
        testBookingTwo.setStatus(Status.APPROVED);
        testBookingThree.setStart(LocalDateTime.now().plusHours(1));
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStartIsAfterAndStatusIsOrderByStartDesc(testUser.getId(),
                LocalDateTime.now(), Status.WAITING, pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 1);
        assertEquals(bookings, List.of(testBookingThree));
    }

    @Test
    public void findAllByBookerIdAndStartIsAfterAndStatusIsOrderByStartDesc_whenUserWithoutBookings_thenReturnEmptyList() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStartIsAfterAndStatusIsOrderByStartDesc(testUserThree.getId(),
                LocalDateTime.now(), Status.WAITING, pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 0);
    }

    //CURRENT
    @Test
    public void findAllByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc_whenDataIsCorrectAndStateCurrent_thenReturnBookingList() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(testUser.getId(),
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings, List.of(testBookingThree, testBookingTwo));
    }

    @Test
    public void findAllByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc_whenUserWithoutBookings_thenReturnEmptyList() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(testUserThree.getId(),
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 0);
    }

    //PAST
    @Test
    public void findAllByBookerIdAndEndIsBeforeOrderByStartDesc_whenDataIsCorrectAndStateCurrent_thenReturnBookingList() {
        testBookingTwo.setEnd(LocalDateTime.now().minusHours(10));
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(testUser.getId(),
                LocalDateTime.now(), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 1);
        assertEquals(bookings, List.of(testBookingTwo));
    }

    @Test
    public void findAllByBookerIdAndEndIsBeforeOrderByStartDesc_whenUserWithoutBookings_thenReturnEmptyList() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(testUserThree.getId(),
                LocalDateTime.now(), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 0);
    }

    //FUTURE
    @Test
    public void findAllByBookerIdAndStartIsAfterOrderByStartDesc_whenDataIsCorrectAndStateFuture_thenReturnBookingList() {
        testBookingThree.setStart(LocalDateTime.now().plusDays(1));
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(testUser.getId(),
                LocalDateTime.now(), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 1);
        assertEquals(bookings, List.of(testBookingThree));
    }

    @Test
    public void findAllByBookerIdAndStartIsAfterOrderByStartDesc_whenUserWithoutBookings_thenReturnEmptyList() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(testUserThree.getId(),
                LocalDateTime.now(), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 0);
    }

    //REJECTED
    @Test
    public void findAllByBookerIdAndStatusIsOrderByStartDesc_whenDataIsCorrectAndStatusRejected_thenReturnBookingList() {
        testBookingTwo.setStatus(Status.REJECTED);
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStatusIsOrderByStartDesc(testUser.getId(),
                Status.REJECTED, pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 1);
        assertEquals(bookings, List.of(testBookingTwo));
    }

    @Test
    public void findAllByBookerIdAndStatusIsOrderByStartDesc_whenUserWithoutBookings_thenReturnEmptyList() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStatusIsOrderByStartDesc(testUserThree.getId(),
                Status.REJECTED, pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 0);
    }

    //Получение всех бронирований для все вещей пользователя:
    // ALL
    @Test
    public void findAllBookingsByOwnerId_whenDataIsCorrectAndStateAll_thenReturnBookingList() {
        TypedQuery<Booking> query = entityManager.getEntityManager()
                .createQuery("SELECT b FROM Booking b " +
                        "INNER JOIN Item i ON b.item.id = i.id " +
                        "WHERE i.owner.id = :ownerId " +
                        "ORDER BY b.start DESC", Booking.class);
        List<Booking> resultList = query.setParameter("ownerId", testUserTwo.getId()).getResultList();
        assertNotNull(resultList);
        assertEquals(resultList.size(), 2);
        assertEquals(resultList, List.of(testBookingThree, testBookingTwo));

        List<Booking> bookings = bookingRepository.findAllBookingsByOwnerId(testUserTwo.getId(), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings, List.of(testBookingThree, testBookingTwo));
    }

    @Test
    public void findAllBookingsByOwnerId_whenOwnerWithoutBookings_thenReturnEmptyList() {
        List<Booking> bookings = bookingRepository.findAllBookingsByOwnerId(testUserThree.getId(), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 0);
    }

    //WAITING
    @Test
    public void findAllBookingsByOwnerIdWithStatusWaiting_whenDataIsCorrectAndStatusWaiting_thenReturnBookingList() {
        testBookingTwo.setStatus(Status.APPROVED);
        testBookingThree.setStart(LocalDateTime.now().plusHours(1));
        TypedQuery<Booking> query = entityManager.getEntityManager()
                .createQuery("SELECT b FROM Booking b " +
                        "INNER JOIN Item i ON b.item.id = i.id " +
                        "WHERE i.owner.id = :ownerId " +
                        "AND b.start > :time AND b.status = :status " +
                        "ORDER BY b.start DESC", Booking.class);
        List<Booking> resultList = query.setParameter("ownerId", testUserTwo.getId())
                .setParameter("time", LocalDateTime.now())
                .setParameter("status", Status.WAITING).getResultList();
        assertNotNull(resultList);
        assertEquals(resultList.size(), 1);
        assertEquals(resultList.get(0), testBookingThree);

        List<Booking> bookings = bookingRepository.findAllBookingsByOwnerIdWithStatusWaiting(testUserTwo.getId(),
                LocalDateTime.now(), Status.WAITING, pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 1);
        assertEquals(bookings, List.of(testBookingThree));
    }

    @Test
    public void findAllBookingsByOwnerIdWithStatusWaiting_whenUserWithoutBookings_thenReturnEmptyList() {
        List<Booking> bookings = bookingRepository.findAllBookingsByOwnerIdWithStatusWaiting(testUserThree.getId(),
                LocalDateTime.now(), Status.WAITING, pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 0);
    }

    //CURRENT
    @Test
    public void findAllBookingsByOwnerIdWithStatusCurrent_whenDataIsCorrectAndStateCurrent_thenReturnBookingList() {
        TypedQuery<Booking> query = entityManager.getEntityManager()
                .createQuery("SELECT b FROM Booking b " +
                        "INNER JOIN Item i ON b.item.id = i.id " +
                        "WHERE i.owner.id = :ownerId " +
                        "AND :time BETWEEN b.start AND b.end " +
                        "ORDER BY b.start DESC", Booking.class);
        List<Booking> resultList = query.setParameter("ownerId", testUserTwo.getId())
                .setParameter("time", LocalDateTime.now()).getResultList();
        assertNotNull(resultList);
        assertEquals(resultList.size(), 2);
        assertEquals(resultList, List.of(testBookingThree, testBookingTwo));

        List<Booking> bookings = bookingRepository.findAllBookingsByOwnerIdWithStatusCurrent(testUserTwo.getId(),
                LocalDateTime.now(), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings, List.of(testBookingThree, testBookingTwo));
    }

    @Test
    public void findAllBookingsByOwnerIdWithStatusCurrent_whenOwnerWithoutBookings_thenReturnEmptyList() {
        List<Booking> bookings = bookingRepository.findAllBookingsByOwnerIdWithStatusCurrent(testUserThree.getId(),
                LocalDateTime.now(), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 0);
    }

    //PAST
    @Test
    public void findAllBookingsByOwnerIdWithStatusPast_whenDataIsCorrectAndStateCurrent_thenReturnBookingList() {
        testBookingTwo.setEnd(LocalDateTime.now().minusHours(10));
        testBookingThree.setStart(LocalDateTime.now().plusDays(1));
        TypedQuery<Booking> query = entityManager.getEntityManager()
                .createQuery("SELECT b FROM Booking b " +
                        "INNER JOIN Item i ON b.item.id = i.id " +
                        "WHERE i.owner.id = :ownerId " +
                        "AND b.end < :time " +
                        "ORDER BY b.start DESC", Booking.class);
        List<Booking> resultList = query.setParameter("ownerId", testUserTwo.getId())
                .setParameter("time", LocalDateTime.now()).getResultList();
        assertNotNull(resultList);
        assertEquals(resultList.size(), 1);
        assertEquals(resultList.get(0), testBookingTwo);

        List<Booking> bookings = bookingRepository.findAllBookingsByOwnerIdWithStatusPast(testUserTwo.getId(),
                LocalDateTime.now(), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 1);
        assertEquals(bookings, List.of(testBookingTwo));
    }

    @Test
    public void findAllBookingsByOwnerIdWithStatusPast_whenOwnerWithoutBookings_thenReturnEmptyList() {
        List<Booking> bookings = bookingRepository.findAllBookingsByOwnerIdWithStatusPast(testUserThree.getId(),
                LocalDateTime.now(), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 0);
    }

    //FUTURE
    @Test
    public void findAllBookingsByOwnerIdWithStatusFuture_whenDataIsCorrectAndStateFuture_thenReturnBookingList() {
        testBookingThree.setStart(LocalDateTime.now().plusDays(1));
        TypedQuery<Booking> query = entityManager.getEntityManager()
                .createQuery("SELECT b FROM Booking b " +
                        "INNER JOIN Item i ON b.item.id = i.id " +
                        "WHERE i.owner.id = :ownerId " +
                        "AND b.start > :time " +
                        "ORDER BY b.start DESC", Booking.class);
        List<Booking> resultList = query.setParameter("ownerId", testUserTwo.getId())
                .setParameter("time", LocalDateTime.now()).getResultList();
        assertNotNull(resultList);
        assertEquals(resultList.size(), 1);
        assertEquals(resultList.get(0), testBookingThree);

        List<Booking> bookings = bookingRepository.findAllBookingsByOwnerIdWithStatusFuture(testUserTwo.getId(),
                LocalDateTime.now(), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 1);
        assertEquals(bookings, List.of(testBookingThree));
    }

    @Test
    public void findAllBookingsByOwnerIdWithStatusFuture_whenOwnerWithoutBookings_thenReturnEmptyList() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(testUserThree.getId(),
                LocalDateTime.now(), pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 0);
    }

    //REJECTED
    @Test
    public void findAllBookingsByOwnerIdWithStatusRejected_whenDataIsCorrectAndStatusRejected_thenReturnBookingList() {
        testBookingTwo.setStatus(Status.REJECTED);
        TypedQuery<Booking> query = entityManager.getEntityManager()
                .createQuery("SELECT b FROM Booking b " +
                        "INNER JOIN Item i ON b.item.id = i.id " +
                        "WHERE i.owner.id = :ownerId " +
                        "AND b.status = :status " +
                        "ORDER BY b.start DESC", Booking.class);
        List<Booking> resultList = query.setParameter("ownerId", testUserTwo.getId())
                .setParameter("status", Status.REJECTED).getResultList();
        assertNotNull(resultList);
        assertEquals(resultList.size(), 1);
        assertEquals(resultList.get(0), testBookingTwo);

        List<Booking> bookings = bookingRepository.findAllBookingsByOwnerIdWithStatusRejected(testUserTwo.getId(),
                Status.REJECTED, pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 1);
        assertEquals(bookings, List.of(testBookingTwo));
    }

    @Test
    public void findAllBookingsByOwnerIdWithStatusRejected_whenUserWithoutBookings_thenReturnEmptyList() {
        List<Booking> bookings = bookingRepository.findAllBookingsByOwnerIdWithStatusRejected(testUserThree.getId(),
                Status.REJECTED, pageRequest);
        assertNotNull(bookings);
        assertEquals(bookings.size(), 0);
    }

    //Для остального:
    @Test
    public void findFirstByBookerAndItemAndEndIsBeforeOrderByEndDesc_whenDataIsCorrect_thenReturnBooking() {
        Booking booking = bookingRepository.findFirstByBookerAndItemAndEndIsBeforeOrderByEndDesc(testUser,
                testItemTwo, LocalDateTime.now().plusDays(2));
        assertNotNull(booking);
        assertEquals(booking, testBookingTwo);
    }

    @Test
    public void findAllBookingsItem_whenDataIsCorrect_thenReturnBookingList() {
        TypedQuery<Booking> query = entityManager.getEntityManager()
                .createQuery("SELECT b FROM Booking b " +
                        "INNER JOIN Item i ON b.item.id = i.id " +
                        "WHERE i.id = :itemId " +
                        "ORDER BY b.start DESC", Booking.class);
        List<Booking> resultList = query.setParameter("itemId", testItemTwo.getId()).getResultList();
        assertNotNull(resultList);
        assertEquals(resultList.size(), 2);
        assertEquals(resultList, List.of(testBookingThree, testBookingTwo));

        List<Booking> bookings = bookingRepository.findAllBookingsItem(testItemTwo.getId());
        assertNotNull(bookings);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings, List.of(testBookingThree, testBookingTwo));
    }
}