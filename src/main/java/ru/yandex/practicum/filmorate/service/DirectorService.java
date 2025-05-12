package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public List<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public Director getDirectorById(int id) {
        return directorStorage.getDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id " + id + " не найден"));
    }

    public Director addDirector(Director director) {
        validateDirector(director);
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        validateDirector(director);
        getDirectorById(director.getId());
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(int id) {
        getDirectorById(id);
        directorStorage.deleteDirector(id);
    }

    private void validateDirector(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new ValidationException("Имя режиссёра не может быть пустым.");
        }
    }
}