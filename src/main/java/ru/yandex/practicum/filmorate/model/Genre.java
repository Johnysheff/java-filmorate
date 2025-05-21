package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genre implements Comparable {
    private Integer id;
    private String name;

    public Genre(Integer id) {
        this.id = id;
    }

    @Override
    public int compareTo(Object o) {
        return this.getId().compareTo(((Genre) o).getId());
    }
}