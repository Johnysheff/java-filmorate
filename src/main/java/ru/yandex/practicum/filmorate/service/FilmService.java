package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage,
                       UserStorage userStorage,
                       MpaStorage mpaStorage,
                       GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        Film savedFilm = filmStorage.addFilm(film);
        genreStorage.addGenresToFilm(savedFilm.getId(), film.getGenres());
        return savedFilm;
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        Optional<Film> foundedFilm = filmStorage.getFilmById(film.getId());
        if (foundedFilm.isEmpty()) {
            throw new NotFoundException("Фильм не найден");
        }
        Film updatedFilm = filmStorage.updateFilm(film);
        genreStorage.addGenresToFilm(updatedFilm.getId(), film.getGenres());
        return updatedFilm;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id).orElseThrow(() ->
                new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public Film deleteFilm(Integer filmId) {
        Film deletedFilm = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
        filmStorage.deleteFilm(deletedFilm);
        return deletedFilm;
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

    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        return filmStorage.getPopularFilmsWithFilters(count, genreId, year);
    }

    public List<Film> getCommonFilms(int userId1, int userId2) {
        List<Film> films = filmStorage.getCommonFilms(userId1, userId2);
        if (films.isEmpty()) {
            return films;
        }
        Map<Integer, List<Genre>> filmGenres = filmStorage.getAllGenres(films);
        films.forEach(film -> {
            int filmId = film.getId();
            film.setGenres(filmGenres.getOrDefault(filmId, new ArrayList<>()));
        });
        return films;
    }

    private void validateFilm(Film film) {

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата релиза должна быть не раньше 28 декабря 1895 года.");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
        }

        if (film.getMpa() != null) {
            MpaRating mpaRating = mpaStorage.getMpaRatingById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Передан несуществующий id рейтинга"));
            film.setMpa(mpaRating);
        } else {
            throw new ValidationException("MPA рейтинг не может быть пустым.");
        }

        if (!CollectionUtils.isEmpty(film.getGenres())) {
            Set<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            List<Genre> validGenres = genreStorage.getGenresByIds(genreIds.stream().toList());
            if (validGenres.size() != genreIds.size()) {
                throw new NotFoundException("Некоторые жанры не найдены.");
            }
            film.setGenres(validGenres.stream().sorted().toList());
        } else {
            film.setGenres(Collections.emptyList());
        }
    }

}