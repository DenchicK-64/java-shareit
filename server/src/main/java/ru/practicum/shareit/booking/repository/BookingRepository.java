package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.PageRequest;
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
    List<Booking> findAllByBookerIdOrderByStartDesc(Long id, PageRequest pageRequest);

    List<Booking> findAllByBookerIdAndStatusIsOrderByStartDesc(Long id, Status status, PageRequest pageRequest);

    List<Booking> findAllByBookerIdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(Long id,
                                                                                 LocalDateTime end,
                                                                                 LocalDateTime start,
                                                                                 PageRequest pageRequest);

    List<Booking> findAllByBookerIdAndEndIsBeforeOrderByStartDesc(Long id, LocalDateTime time, PageRequest pageRequest);

    List<Booking> findAllByBookerIdAndStartIsAfterOrderByStartDesc(Long id, LocalDateTime time, PageRequest pageRequest);

    List<Booking> findAllByBookerIdAndStartIsAfterAndStatusIsOrderByStartDesc(Long bookerId,
                                                                              LocalDateTime start,
                                                                              Status status,
                                                                              PageRequest pageRequest);
    //Получение всех бронирований для все вещей пользователя:

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingsByOwnerId(Long ownerId, PageRequest pageRequest);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND :time BETWEEN b.start AND b.end " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingsByOwnerIdWithStatusCurrent(Long ownerId, LocalDateTime time, PageRequest pageRequest);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.end < :time " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingsByOwnerIdWithStatusPast(Long ownerId, LocalDateTime time, PageRequest pageRequest);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.start > :time " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingsByOwnerIdWithStatusFuture(Long ownerId, LocalDateTime time, PageRequest pageRequest);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.start > :time AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingsByOwnerIdWithStatusWaiting(Long ownerId, LocalDateTime time, Status status, PageRequest pageRequest);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingsByOwnerIdWithStatusRejected(Long ownerId, Status status, PageRequest pageRequest);

    // Для остального:
    Booking findFirstByBookerAndItemAndEndIsBeforeOrderByEndDesc(User user, Item item, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "INNER JOIN Item i ON b.item.id = i.id " +
            "WHERE i.id = :itemId " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingsItem(Long itemId);
}