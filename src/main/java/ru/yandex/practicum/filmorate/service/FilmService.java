package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        Optional<Film> foundedFilm = filmStorage.getFilmById(film.getId());
        if (foundedFilm.isEmpty()) {
            throw new NotFoundException("Фильм не найден");
        }
        return filmStorage.updateFilm(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id).orElseThrow(() ->
                new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public void addLike(int filmId, int userId) {
        Optional<Film> foundedFilm = filmStorage.getFilmById(filmId);
        if (foundedFilm.isEmpty()) {
            throw new NotFoundException("Фильм не найден");
        }
        Optional<User> foundedUser = userStorage.getUserById(userId);
        if (foundedUser.isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        Optional<Film> foundedFilm = filmStorage.getFilmById(filmId);
        if (foundedFilm.isEmpty()) {
            throw new NotFoundException("Фильм не найден");
        }
        Optional<User> foundedUser = userStorage.getUserById(userId);
        if (foundedUser.isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата релиза должна быть не раньше 28 декабря 1895 года.");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
        }
    }
}