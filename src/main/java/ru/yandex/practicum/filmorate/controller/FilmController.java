package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;
    private final FilmDbStorage filmDbStorage;

    @Autowired
    public FilmController(FilmService filmService, FilmDbStorage filmDbStorage) {
        this.filmService = filmService;
        this.filmDbStorage = filmDbStorage;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        Film updatedFilm = filmService.updateFilm(film);
        if (updatedFilm.getDirectors() == null) {
            updatedFilm.setDirectors(Collections.emptyList());
        }
        return updatedFilm;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        Film film = filmService.getFilmById(id);
        if (film.getGenres() == null) {
            film.setGenres(Collections.emptyList());
        }
        if (film.getDirectors() == null) {
            film.setDirectors(Collections.emptyList());
        }
        return film;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
    }

    //Получаем список популярных фильмов
    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @DeleteMapping("/{filmId}")
    public Film deleteFilm(@PathVariable int filmId) {
        return filmService.deleteFilm(filmId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable int directorId,
                                         @RequestParam String sortBy) {
        List<Film> films = filmService.getFilmsByDirector(directorId, sortBy);
        films.forEach(film -> {
            if (film.getDirectors() == null) {
                film.setDirectors(Collections.emptyList());
            }
        });
        return films;
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam String query, @RequestParam String by) {
        if (query == null || by == null) {
            return List.of();
        }

        switch (by.toLowerCase()) {
            case "director":
                return filmDbStorage.searchFilmsByDirector(query);
            case "title":
                return filmDbStorage.searchFilmsByTitle(query);
            case "title,director":
                return filmDbStorage.searchFilmsByTitleAndDirector(query,query);
            default:
                return List.of();
        }
    }
}