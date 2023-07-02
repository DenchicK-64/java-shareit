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
    List<Booking> findAllBookingsByOwnerId(long ownerId);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND :time BETWEEN b.start AND b.end " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingsByOwnerIdWithStatusCurrent(long ownerId, LocalDateTime time);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.end < :time " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingsByOwnerIdWithStatusPast(long ownerId, LocalDateTime time);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.start > :time " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingsByOwnerIdWithStatusFuture(long ownerId, LocalDateTime time);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.start > :time AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingsByOwnerIdWithStatusWaiting(long ownerId, LocalDateTime time, Status status);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingsByOwnerIdWithStatusRejected(long ownerId, Status status);

    // Поиск бронирования по id и id автора бронирования:

    @Query("SELECT b FROM Booking b " +
            "WHERE b.id = :bookingId " +
            "AND b.booker.id = :bookerId ")
    Booking findBookingByIdAndBookerId(long bookingId, long bookerId);

    // Поиск бронирования по id и id владельца вещи:

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.id = :bookingId ")
    Booking findBookingByIdAndOwnerId(long bookingId, long ownerId);

    // Для остального:
    Booking findFirstByBookerAndItemAndEndIsBeforeOrderByEndDesc(User user, Item item, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.id = :itemId " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingsItem(long itemId);
}