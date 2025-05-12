package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    List<Director> getAllDirectors();

    Optional<Director> getDirectorById(int id);

    Director addDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(int id);

    List<Director> getDirectorsByFilmId(int filmId);

    void addDirectorsToFilm(int filmId, List<Director> directors);

    void deleteDirectorsFromFilm(int filmId);
}