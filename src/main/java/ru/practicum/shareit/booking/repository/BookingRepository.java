package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    //Получение всех бронирований пользователя:
    List<Booking> findAllByBookerIdOrderByStartDesc(long id);

    List<Booking> findAllByBookerIdAndStatusIsOrderByStartDesc(long id, Status status);

    List<Booking> findAllByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(long id,
                                                                                 LocalDateTime end,
                                                                                 LocalDateTime start);

    List<Booking> findAllByBookerIdAndEndIsBeforeOrderByStartDesc(long id, LocalDateTime time);

    List<Booking> findAllByBookerIdAndStartIsAfterOrderByStartDesc(long id, LocalDateTime time);

    List<Booking> findAllByBookerIdAndStartIsAfterAndStatusIsOrderByStartDesc(long bookerId,
                                                                              LocalDateTime start,
                                                                              Status status);
    //Получение всех бронирований для все вещей пользователя:

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdOrderByStartDesc(Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND :time BETWEEN b.start AND b.end " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(Long ownerId, LocalDateTime time);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.end < :time " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdAndEndIsBeforeOrderByStartDesc(Long ownerId, LocalDateTime time);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.start > :time " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdAndStartIsAfterOrderByStartDesc(Long ownerId, LocalDateTime time);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.start > :time AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdAndStartIsAfterAndStatusIsOrderByStartDesc(Long ownerId, LocalDateTime time, Status status);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdAndStatusIsOrderByStartDesc(Long ownerId, Status status);

    // Для остального:
    Booking findFirstByBookerAndItemAndEndIsBeforeOrderByEndDesc(User user, Item item, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.id = :itemId " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingsItem(long itemId);
}