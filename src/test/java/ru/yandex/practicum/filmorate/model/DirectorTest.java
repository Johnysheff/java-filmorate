package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DirectorTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validateDirectorWithValidData() {
        Director director = new Director(1, "Valid Name");
        Set<ConstraintViolation<Director>> violations = validator.validate(director);
        assertTrue(violations.isEmpty(), "Режиссёр с корректными данными должен пройти валидацию");
    }

    @Test
    void validateDirectorWithEmptyName() {
        Director director = new Director(1, "");
        Set<ConstraintViolation<Director>> violations = validator.validate(director);
        assertFalse(violations.isEmpty(), "Режиссёр с пустым именем не должен проходить валидацию");
        assertEquals("Имя режиссёра не может быть пустым.", violations.iterator().next().getMessage());
    }

    @Test
    void validateDirectorWithNullName() {
        Director director = new Director(1, null);
        Set<ConstraintViolation<Director>> violations = validator.validate(director);
        assertFalse(violations.isEmpty(), "Режиссёр с null именем не должен проходить валидацию");
    }
}