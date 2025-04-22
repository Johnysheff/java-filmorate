package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();

    Optional<Film> getFilmById(int id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> getPopularFilms(int count);
}