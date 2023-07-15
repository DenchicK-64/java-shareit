package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDtoWithItems {
    private Long id;
    @NotBlank(message = "Описание запроса должно содержать текст")
    private String description;
    private UserDto requester;
    private LocalDateTime created;
    private List<ItemDto> items;
}