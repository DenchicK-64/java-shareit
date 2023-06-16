package ru.practicum.shareit.exceptions;

public class OperationAccessException extends RuntimeException {
    public OperationAccessException(String message) {
        super(message);
    }
}