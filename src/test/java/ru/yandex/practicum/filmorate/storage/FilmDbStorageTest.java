package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = "classpath:clear_tables.sql")
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final DirectorStorage directorStorage;
    private final UserStorage userStorage;

    @Test
    void shouldAddAndFindFilmById() {
        Film film = new Film("Новый фильм", "Описание фильма",
                LocalDate.of(2021, 1, 1), 130);
        film.setMpa(new MpaRating(1, "G"));
        film.setGenres(List.of());

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

    @Test
    void shouldAddFilmWithDirectors() {
        // Сначала создаем режиссера
        Director director = directorStorage.addDirector(new Director(null, "Тестовый Режиссер"));

        Film film = new Film("Фильм с режиссером", "Описание",
                LocalDate.of(2021, 1, 1), 120);
        film.setMpa(new MpaRating(1, "G"));
        film.setDirectors(List.of(director));

        Film addedFilm = filmStorage.addFilm(film);
        assertThat(addedFilm.getDirectors())
                .hasSize(1)
                .extracting(Director::getName)
                .containsExactly("Тестовый Режиссер");
    }

    @Test
    void shouldGetFilmsByDirectorSortedByYear() {
        // Создаем режиссера
        Director director = new Director(null, "Тестовый Режиссер");
        Director savedDirector = directorStorage.addDirector(director);

        // Создаем фильмы
        Film film1 = new Film("Фильм 1", "Описание",
                LocalDate.of(2020, 1, 1), 120);
        film1.setMpa(new MpaRating(1, "G"));
        filmStorage.addFilm(film1);

        Film film2 = new Film("Фильм 2", "Описание",
                LocalDate.of(2021, 1, 1), 120);
        film2.setMpa(new MpaRating(1, "G"));
        filmStorage.addFilm(film2);

        // Связываем фильмы с режиссером
        directorStorage.addDirectorsToFilm(film1.getId(), List.of(savedDirector));
        directorStorage.addDirectorsToFilm(film2.getId(), List.of(savedDirector));

        List<Film> films = filmStorage.getFilmsByDirectorSortedByYear(savedDirector.getId());
        assertThat(films)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactly("Фильм 1", "Фильм 2");
    }

    @Test
    void shouldGetFilmsByDirectorSortedByLikes() {
        User user = new User("user1", "vasiya", "vasiya1",LocalDate.of(2020,1,1));
        int userId = userStorage.addUser(user).getId();

        // Создаем режиссера
        Director director = directorStorage.addDirector(new Director(null, "Тестовый Режиссер"));

        // Создаем фильмы
        Film film1 = new Film("Фильм 1", "Описание",
                LocalDate.of(2020, 1, 1), 120);
        film1.setMpa(new MpaRating(1, "G"));
        filmStorage.addFilm(film1);

        Film film2 = new Film("Фильм 2", "Описание",
                LocalDate.of(2021, 1, 1), 120);
        film2.setMpa(new MpaRating(1, "G"));
        filmStorage.addFilm(film2);

        // Связываем фильмы с режиссером
        directorStorage.addDirectorsToFilm(film1.getId(), List.of(director));
        directorStorage.addDirectorsToFilm(film2.getId(), List.of(director));

        // Добавляем лайки
        filmStorage.addLike(film2.getId(), userId);

        List<Film> films = filmStorage.getFilmsByDirectorSortedByLikes(director.getId());
        assertThat(films)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactly("Фильм 2", "Фильм 1");
    }
}