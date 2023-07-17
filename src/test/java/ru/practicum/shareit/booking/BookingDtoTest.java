package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.enums.Status;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingDtoTest {
    @Autowired
    private JacksonTester<BookingRequestDto> json;
    BookingRequestDto testBookingRequestDto;

    @BeforeEach
    public void setUp() {
        testBookingRequestDto = new BookingRequestDto(1L, 1L, LocalDateTime.now(),
                LocalDateTime.now().minusDays(2), Status.WAITING);
    }

    @Test
    public void serialize() throws IOException {
        JsonContent<BookingRequestDto> bookingRequestDto = json.write(testBookingRequestDto);
        assertThat(bookingRequestDto).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(bookingRequestDto).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(bookingRequestDto).extractingJsonPathStringValue("$.status").isEqualTo(Status.WAITING.toString());
    }

    @Test
    public void deserialize() throws IOException {
        String jsonContent = String.format("{\"id\":\"%s\", \"itemId\": \"%s\", \"status\": \"%s\"}",
                testBookingRequestDto.getId(), testBookingRequestDto.getItemId(), testBookingRequestDto.getStatus());
        BookingRequestDto bookingRequestDto = this.json.parse(jsonContent).getObject();
        assertThat(bookingRequestDto.getId()).isEqualTo(1L);
        assertThat(bookingRequestDto.getItemId()).isEqualTo(1L);
        assertThat(bookingRequestDto.getStatus()).isEqualTo(Status.WAITING);
    }
}