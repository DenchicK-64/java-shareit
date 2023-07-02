package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortBookingDto {
    private long id;
    private long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
}