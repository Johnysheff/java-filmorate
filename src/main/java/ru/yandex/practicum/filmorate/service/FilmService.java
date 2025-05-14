package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.model.feed.Event;
import ru.yandex.practicum.filmorate.model.feed.EventOperation;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
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
    private final DirectorStorage directorStorage;
    private final EventService eventService;

    @Autowired
    public FilmService(FilmStorage filmStorage,
                       UserStorage userStorage,
                       MpaStorage mpaStorage,
                       GenreStorage genreStorage,
                       DirectorStorage directorStorage,
                       EventService eventService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
        this.eventService = eventService;
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        Film savedFilm = filmStorage.addFilm(film);
        if (film.getGenres() != null) {
            genreStorage.addGenresToFilm(savedFilm.getId(), film.getGenres());
        }
        if (film.getDirectors() != null) {
            directorStorage.addDirectorsToFilm(savedFilm.getId(), film.getDirectors());
        }
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
        directorStorage.deleteDirectorsFromFilm(updatedFilm.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorStorage.addDirectorsToFilm(updatedFilm.getId(), film.getDirectors());
            updatedFilm.setDirectors(new ArrayList<>(film.getDirectors()));
        } else {
            updatedFilm.setDirectors(Collections.emptyList());
        }

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

        // Добавляем событие
        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(EventOperation.ADD)
                .entityId(filmId)
                .build();
        eventService.addEvent(event);

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

        // Добавляем событие
        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(EventOperation.REMOVE)
                .entityId(filmId)
                .build();
        eventService.addEvent(event);

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

            if (film.getMpa() != null && film.getMpa().getId() != null) {
                Optional<MpaRating> rating = mpaStorage.getMpaRatingById(film.getMpa().getId());
                rating.ifPresent(film::setMpa);
            }
        });
        return films;
    }

        public List<Film> getFilmsByDirector ( int directorId, String sortBy){

            directorStorage.getDirectorById(directorId)
                    .orElseThrow(() -> new NotFoundException("Режиссёр с id " + directorId + " не найден"));

            List<Film> films;
            if (sortBy.equals("year")) {
                films = filmStorage.getFilmsByDirectorSortedByYear(directorId);
            } else if (sortBy.equals("likes")) {
                films = filmStorage.getFilmsByDirectorSortedByLikes(directorId);
            } else {
                throw new ValidationException("Неправильный параметр сортировки: " + sortBy);
            }

            films.forEach(film -> {
                List<Director> directors = directorStorage.getDirectorsByFilmId(film.getId());
                film.setDirectors(directors != null ? directors : Collections.emptyList());
            });

            return films;
        }

        private void validateFilm (Film film){

            if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
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

                List<Genre> validGenres = genreStorage.getGenresByIds(new ArrayList<>(genreIds));
                if (validGenres.size() != genreIds.size()) {
                    throw new NotFoundException("Некоторые жанры не найдены.");
                }
                film.setGenres(validGenres.stream().sorted().toList());
            } else {
                film.setGenres(Collections.emptyList());
            }

            if (!CollectionUtils.isEmpty(film.getDirectors())) {
                Set<Integer> directorIds = film.getDirectors().stream()
                        .map(Director::getId)
                        .collect(Collectors.toSet());

                for (Integer directorId : directorIds) {
                    directorStorage.getDirectorById(directorId)
                            .orElseThrow(() -> new NotFoundException("Режиссёр с id " + directorId + " не найден"));
                }
            }
        }
    }