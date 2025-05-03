package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
@AutoConfigureTestDatabase
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = "classpath:clear_tables.sql")
class FilmDbStorageTest {
    @Autowired
    private FilmDbStorage filmStorage;

    @Test
    void shouldAddAndFindFilmById() {
        Film film = new Film("Новый фильм", "Описание фильма",
                LocalDate.of(2021, 1, 1), 130);
        film.setMpa(new MpaRating(1, "G"));
        film.setGenres(Collections.emptyList());

        Film addedFilm = filmStorage.addFilm(film);
        Optional<Film> foundFilm = filmStorage.getFilmById(addedFilm.getId());

        assertThat(foundFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f)
                                .hasFieldOrPropertyWithValue("name", "Новый фильм")
                                .hasFieldOrPropertyWithValue("description", "Описание фильма")
                );
    }
}