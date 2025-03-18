package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validateUserWithValidData() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Пользователь с корректными данными должен пройти валидацию без ошибок");
    }

    @Test
    void validateUserWithInvalidEmail() {
        User user = new User();
        user.setEmail("invalid-email"); // Некорректный email
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пользователь с некорректным email не должен проходить валидацию.");
        assertEquals("Электронная почта должна быть корректной.", violations.iterator().next().getMessage());
    }
}