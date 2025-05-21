package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(of = {"reviewId"})
public class Review {
    private int reviewId;

    @NotNull(message = "ID фильма обязателен")
    private Integer filmId;

    @NotNull(message = "ID пользователя обязателен")
    private Integer userId;

    @NotBlank(message = "Отзыв не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва должен быть указан")
    private Boolean isPositive;
    private int useful;
}
