package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //Ловим ошибки валидации (статус 400)
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException e) {
        log.warn("Ошибка валидации: {}", e.getMessage());
        return new ErrorResponse("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMessage();

        if (message.contains("REFERENCES PUBLIC.FILMS")) {
            return new ErrorResponse("Фильм не найден", ex.getMessage());
        } else if (message.contains("REFERENCES PUBLIC.USERS")) {
            return new ErrorResponse("Пользователь не найден", ex.getMessage());
        }

        return new ErrorResponse("Ошибка целостности данных", ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body("Пропущен обязательный параметр: " + ex.getParameterName());
    }

    //Ловим ошибки валидации аннотаций (статус 400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(final MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Ошибка валидации данных: {}", errorMessage);  // Логирование
        return new ErrorResponse("Некорректные данные", errorMessage);
    }

    //Ловим случаи "не найдено" (статус 404)
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        log.warn("Объект не найден: {}", e.getMessage());
        return new ErrorResponse("Объект не найден", e.getMessage());
    }

    // Ловим все необработанные исключения (статус 500)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalError(final Exception e) {
        log.error("Внутренняя ошибка сервера: ", e);  // Логирование с полным стектрейсом
        return new ErrorResponse("Внутренняя ошибка сервера", e.getMessage());
    }
}