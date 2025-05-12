package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();

    Optional<Film> getFilmById(int id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> getPopularFilms(int count);

    void deleteFilm(Film film);

    List<Film> getPopularFilmsWithFilters(int count, Integer genreId, Integer year);

    List<Film> getCommonFilms(int userId, int userId2);

    Map<Integer, List<Genre>> getAllGenres(Collection<Film> films);

    List<Film> getFilmsByDirectorSortedByYear(int directorId);

    List<Film> getFilmsByDirectorSortedByLikes(int directorId);
}