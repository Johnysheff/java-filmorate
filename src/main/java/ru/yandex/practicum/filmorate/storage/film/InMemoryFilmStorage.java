package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private final Map<Integer, Set<Integer>> likes = new HashMap<>();
    private int idCounter = 1;

    @Override
    public Film addFilm(Film film) {
        film.setId(idCounter++);
        films.put(film.getId(), film);
        likes.put(film.getId(), new HashSet<>());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void addLike(int filmId, int userId) {
        likes.get(filmId).add(userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        likes.get(filmId).remove(userId);
    }

    //Сортируем фильмы по количеству лайков
    @Override
    public List<Film> getPopularFilms(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(likes.get(f2.getId()).size(), likes.get(f1.getId()).size()))
                .limit(count)
                .toList();
    }
}