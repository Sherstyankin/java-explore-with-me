package ru.practicum.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ApiError;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.exception.ValidationException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        log.warn("Получен статус 400 Bad request: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Передано невалидное значение.")
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.warn("Получен статус 400 Bad request: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Передано невалидное значение.")
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConversionFailedException(final ConversionFailedException e) {
        log.warn("Получен статус 400 Bad request: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Передано невалидное значение.")
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleValidationException(final EntityNotFoundException e) {
        log.warn("Получен статус 404 Not found: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .reason("Сущность не найдена.")
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataConflictException(final ConflictException e) {
        log.warn("Получен статус 409 Conflict: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Конфликт данных.")
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityConflictException(final DataIntegrityViolationException e) {
        log.warn("Получен статус 409 Conflict: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Конфликт данных.")
                .message(e.getMessage())
                .build();
    }
}
