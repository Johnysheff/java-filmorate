package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final FilmDbStorage filmRepository;
    private final UserDbStorage userStorage;

    public Collection<Film> getRecommendations(Integer userId) {
        log.info("Получение рекомендаций для пользователя {}", userId);

        Set<Integer> userLikedFilms = new HashSet<>(filmRepository.getFilmIdsByUserId(userId));
        log.debug("Пользователь {} лайкнул фильмы: {}", userId, userLikedFilms);

        if (userLikedFilms.isEmpty()) {
            log.info("У пользователя {} нет лайков, рекомендации не найдены", userId);
            return Collections.emptyList();
        }

        Map<Integer, Set<Integer>> similarUsers = findSimilarUsers(userId, userLikedFilms);

        if (similarUsers.isEmpty()) {
            log.info("Для пользователя {} не найдено похожих пользователей", userId);
            return Collections.emptyList();
        }

        Set<Integer> recommendedFilmIds = getRecommendedFilmIds(userLikedFilms, similarUsers);

        return convertToFilms(recommendedFilmIds);
    }

    private Map<Integer, Set<Integer>> findSimilarUsers(Integer userId, Set<Integer> userLikedFilms) {
        Map<Integer, Set<Integer>> similarUsers = new HashMap<>();
        for (Integer filmId : userLikedFilms) {
            List<Integer> usersWhoLikedFilm = filmRepository.getUserIdsByFilmId(filmId);
            for (Integer otherUserId : usersWhoLikedFilm) {
                if (!otherUserId.equals(userId)) {
                    similarUsers.computeIfAbsent(otherUserId, k -> new HashSet<>()).add(filmId);
                }
            }
        }
        return similarUsers;
    }

    private Set<Integer> getRecommendedFilmIds(Set<Integer> userLikedFilms,
                                               Map<Integer, Set<Integer>> similarUsers) {
        int maxIntersection = similarUsers.values().stream()
                .mapToInt(Set::size)
                .max()
                .orElse(0);

        return similarUsers.entrySet().stream()
                .filter(entry -> entry.getValue().size() == maxIntersection)
                .flatMap(entry -> {
                    Set<Integer> films = new HashSet<>(
                            filmRepository.getFilmIdsByUserId(entry.getKey()));
                    films.removeAll(userLikedFilms);
                    return films.stream();
                })
                .collect(Collectors.toSet());
    }

    private List<Film> convertToFilms(Set<Integer> filmIds) {
        return filmIds.stream()
                .map(filmId -> {
                    Optional<Film> filmOpt = filmRepository.getFilmById(filmId);
                    return filmOpt.orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}