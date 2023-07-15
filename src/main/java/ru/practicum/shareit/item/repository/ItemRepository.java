package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long userId, PageRequest pageRequest);

    @Query(" SELECT i FROM Item i WHERE  (upper(i.name) LIKE upper(concat('%', ?1, '%')) " +
            "OR upper(i.description) LIKE upper(concat('%', ?1, '%'))) AND i.available = true")
    List<Item> search(String text, PageRequest pageRequest);

    @Query("SELECT i FROM Item i WHERE i.id = :itemId AND i.owner.id = :ownerId ")
    Item findItemsByIdAndOwnerId(Long itemId, Long ownerId);

    List<Item> findAllByItemRequest(ItemRequest itemRequest);
}