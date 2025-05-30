package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpaRating {
    private Integer id;
    private String name;
    private String description;

    public MpaRating(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}