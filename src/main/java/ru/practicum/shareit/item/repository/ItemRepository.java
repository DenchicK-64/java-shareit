package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(long userId);

    @Query(" SELECT i FROM Item i WHERE  (upper(i.name) LIKE upper(concat('%', ?1, '%')) " +
            "OR upper(i.description) LIKE upper(concat('%', ?1, '%'))) AND i.available = true")
    List<Item> search(String text);

    @Query("SELECT i FROM Item i WHERE i.id = :itemId AND i.owner.id = :ownerId ")
    Item findItemsByIdAndOwnerId(long itemId, long ownerId);
}